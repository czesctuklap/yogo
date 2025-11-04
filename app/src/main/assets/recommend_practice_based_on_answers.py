import sqlite3
import json
import sys
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity

DB_FILE = "yogo_database_git"

CATEGORY_WEIGHTS = {
    "time_of_day": 2.5,
    "intensity": 5.0,
    "level": 7.0,
    "focus_area": 10.0,
    "type": 10.0,
    "goal": 10.0,
    "props": 5.0
}

LEVEL_WEIGHTED = {
    "beginner": {"beginner": 1.0},
    "intermediate": {"intermediate": 1.0, "beginner": 0.5},
    "advanced": {"advanced": 1.0, "intermediate": 0.7, "beginner": 0.4}
}

INTENSITY_HIERARCHY = {
    "low": ["low"],
    "medium": ["medium", "low"],
    "high": ["high", "medium"]
}

PARTIAL_MATCH_WEIGHT = 0.5

def get_duration_range(user_tags):
    if "20minus" in user_tags:
        return (0, 23)
    elif "2045" in user_tags:
        return (20, 46)
    elif "45plus" in user_tags:
        return (45, 999)
    return (0, 999)


def find_best_matches(user_answers_str, top_n=10):
    conn = sqlite3.connect(DB_FILE)
    cur = conn.cursor()

    user_tags = [t.strip().lower() for t in user_answers_str.split(";") if t.strip()]
    if len(user_tags) != 8:
        raise ValueError(
            "User input must contain exactly 8 semicolon-separated tags in order: intensity;props;focus_area;goal;duration_minutes;level;type;time_of_day"
        )

    user_intensity, user_props, user_focus, user_goal, duration_tag, user_level, user_type, user_time = user_tags
    min_duration, max_duration = get_duration_range([duration_tag])

    cur.execute("SELECT practice FROM history;")
    watched_ids = {row[0] for row in cur.fetchall()}

    cur.execute("""
        SELECT p.id, p.title, p.youtube_url, p.duration_minutes, t.name, t.category
        FROM practice p
        JOIN practice_tag pt ON p.id = pt.practice_id
        JOIN tag t ON t.id = pt.tag_id
    """)

    practices = {}
    tags_all = set()
    for pid, title, url, duration, tag_name, category in cur.fetchall():
        if pid not in practices:
            practices[pid] = {
                "id": pid,
                "title": title,
                "youtube_url": url,
                "duration_minutes": duration,
                "tags": {}
            }
        tag_name = tag_name.lower()
        practices[pid]["tags"][category] = tag_name
        tags_all.add((category, tag_name))

    conn.close()

    category_tags = {}
    for category, tag_name in tags_all:
        category_tags.setdefault(category, set()).add(tag_name)

    def encode_tags(tag_dict, user_level=None):
        vector = []
        for category, tag_set in category_tags.items():
            base_weight = CATEGORY_WEIGHTS.get(category, 1.0)
            for tag in sorted(tag_set):
                value = 1.0 if tag_dict.get(category) == tag else 0.0

                if category == "level" and user_level and value == 1.0:
                    level_weights = LEVEL_WEIGHTED.get(user_level, {user_level: 1.0})
                    match_factor = level_weights.get(tag, 0.0)
                    value *= match_factor

                vector.append(value * base_weight)
        return np.array(vector, dtype=float)

    user_tag_dict = {
        "intensity": user_intensity,
        "props": user_props,
        "focus_area": user_focus,
        "goal": user_goal,
        "level": user_level,
        "type": user_type,
        "time_of_day": user_time
    }

    user_vector = encode_tags(user_tag_dict, user_level)

    X = []
    P = []

    for p in practices.values():
        if p["id"] in watched_ids:
            continue

        duration = p["duration_minutes"]
        tags = p["tags"]
        score_bonus = 0.0

        if duration is not None and not (min_duration <= duration <= max_duration):
            continue

        valid = True

        if "level" in tags:
            allowed_levels = LEVEL_WEIGHTED.get(user_level, {user_level: 1.0})
            if tags["level"] not in allowed_levels:
                valid = False

        if valid and "intensity" in tags:
            if user_intensity != "all":
                allowed_intensity = INTENSITY_HIERARCHY.get(user_intensity, [user_intensity])
                if tags["intensity"] not in allowed_intensity:
                    valid = False

        if valid and "props" in tags:
            tag_props = tags["props"]
            if user_props == "all":
                pass
            elif user_props == "none":
                valid = (tag_props == "none")
            elif user_props == "blocks":
                valid = tag_props in ["blocks", "none"]
            elif user_props == "belt":
                valid = tag_props in ["belt", "none"]
            else:
                valid = (tag_props == user_props)

        if valid and "focus_area" in tags:
            if user_focus == "all":
                score_bonus += CATEGORY_WEIGHTS["focus_area"] * PARTIAL_MATCH_WEIGHT
            elif tags["focus_area"] == user_focus:
                score_bonus += CATEGORY_WEIGHTS["focus_area"]
            else:
                score_bonus += CATEGORY_WEIGHTS["focus_area"] * PARTIAL_MATCH_WEIGHT

        if valid and "goal" in tags:
            if user_goal == "all":
                score_bonus += CATEGORY_WEIGHTS["goal"] * PARTIAL_MATCH_WEIGHT
            elif tags["goal"] == user_goal:
                score_bonus += CATEGORY_WEIGHTS["goal"]
            else:
                score_bonus += CATEGORY_WEIGHTS["goal"] * PARTIAL_MATCH_WEIGHT

        if valid and "type" in tags:
            if user_type == "all":
                score_bonus += CATEGORY_WEIGHTS["type"] * PARTIAL_MATCH_WEIGHT
            elif tags["type"] == user_type:
                score_bonus += CATEGORY_WEIGHTS["type"]
            else:
                score_bonus += CATEGORY_WEIGHTS["type"] * PARTIAL_MATCH_WEIGHT

        if not valid:
            continue

        P.append((p, score_bonus))
        X.append(encode_tags(tags, user_level))

    if not X:
        return []

    X = np.array(X)

    sims = cosine_similarity([user_vector], X)[0]

    scored_practices = []
    for (p, score_bonus), sim in zip(P, sims):
        total_score = float(sim) + (score_bonus / 100.0)  # lekki bonus do similarity
        scored_practices.append({
            "title": p["title"],
            "youtube_url": p["youtube_url"],
            "duration_minutes": p["duration_minutes"],
            "score": round(total_score, 4)
        })

    scored_practices.sort(key=lambda x: x["score"], reverse=True)

    return scored_practices[:top_n]


if __name__ == "__main__":
    try:
        if len(sys.argv) > 1 and sys.argv[1].strip():
            user_input = sys.argv[1]
        else:
            print(json.dumps({"error": "Invalid user input (user_input)."}, ensure_ascii=False))
            sys.exit(1)

        results = find_best_matches(user_input)
        print(json.dumps(results, ensure_ascii=False))

    except Exception as e:
        print(json.dumps({"error": f"Error: {str(e)}"}, ensure_ascii=False))
        sys.exit(1)

    """user_input = "low;belt;pregnancy;relaxation;45plus;advanced;power;before_sleep"
    results = find_best_matches(user_input)
    print("TOP DOPASOWANIA:\n")
    for i, r in enumerate(results, 1):
        print(f"{i}. {r['title']} ({r['duration_minutes']} min) - wynik: {r['score']}")
        print(f"   {r['youtube_url']}\n")"""