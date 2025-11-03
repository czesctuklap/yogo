import sqlite3
import json
import sys

DB_FILE = "yogo_database"

# wagi kategorii tagów
CATEGORY_WEIGHTS = {
    "time_of_day": 2.5,
    "intensity": 5.0,
    "level": 5.0,
    "focus_area": 10.0,
    "type": 10.0,
    "goal": 10.0,
    "props": 5.0
}

# relacje pomiędzy poziomami trudności
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

def get_duration_range(user_tags):
    """określa przedział długości na podstawie tagów."""
    if "20minus" in user_tags:
        return (0, 23)
    elif "2045" in user_tags:
        return (20, 46)
    elif "45plus" in user_tags:
        return (45, 999)
    return (0, 999)  # brak ograniczenia

def find_best_matches(user_answers_str, top_n=10):
    conn = sqlite3.connect(DB_FILE)
    cur = conn.cursor()

    user_tags = [t.strip().lower() for t in user_answers_str.split(";") if t.strip()]
    if len(user_tags) != 8:
        raise ValueError(
            "User input must contain exactly 8 semicolon-separated tags in the correct order: intensity;props;focus_area;goal;duration_minutes;level;type;time_of_day")

    user_intensity, user_props, user_focus, user_goal, duration_tag, user_level, user_type, user_time = user_tags

    all_focus = user_focus == "all"
    all_goal = user_goal == "all"
    all_type = user_type == "all"

    min_duration, max_duration = get_duration_range([duration_tag])

    cur.execute("""
        SELECT p.id, p.title, p.youtube_url, p.duration_minutes, t.name, t.category
        FROM practice p
        JOIN practice_tag pt ON p.id = pt.practice_id
        JOIN tag t ON t.id = pt.tag_id
    """)
    practices = {}
    for pid, title, url, duration, tag_name, category in cur.fetchall():
        if pid not in practices:
            practices[pid] = {
                "id": pid,
                "title": title,
                "youtube_url": url,
                "duration_minutes": duration,
                "tags": []
            }
        practices[pid]["tags"].append({"name": tag_name.lower(), "category": category})

    scored_practices = []
    for p in practices.values():
        score = 0
        valid = True

        duration = p["duration_minutes"]
        if duration is not None and not (min_duration <= duration <= max_duration):
            continue

        for tag in p["tags"]:
            tag_name = tag["name"]
            category = tag["category"]

            if category == "time_of_day":
                if tag_name == user_time:
                    score += CATEGORY_WEIGHTS["time_of_day"]
                continue

            # level
            if category == "level":
                if user_level:
                    allowed_levels = LEVEL_WEIGHTED[user_level]
                    if tag_name not in allowed_levels:
                        valid = False
                        break
                    else:
                        score += CATEGORY_WEIGHTS["level"] * allowed_levels[tag_name]

            # props
            elif category == "props":
                if user_props == "none":
                    if tag_name != "none":
                        valid = False
                        break
                elif user_props == "blocks":
                    if tag_name not in ["blocks", "none"]:
                        valid = False
                        break
                elif user_props == "belt":
                    if tag_name not in ["belt", "none"]:
                        valid = False
                        break
                elif user_props == "all":
                    pass

                if tag_name == user_props:
                    score += CATEGORY_WEIGHTS["props"]

            # intensity
            elif category == "intensity":
                if user_intensity == "all":
                    continue
                elif user_intensity:
                    allowed_intensity = INTENSITY_HIERARCHY[user_intensity]
                    if tag_name not in allowed_intensity:
                        valid = False
                        break
                    if tag_name == user_intensity:
                        score += CATEGORY_WEIGHTS["intensity"]

            elif category == "focus_area":
                if all_focus or tag_name in user_tags:
                    score += CATEGORY_WEIGHTS["focus_area"]
            elif category == "goal":
                if all_goal or tag_name in user_tags:
                    score += CATEGORY_WEIGHTS["goal"]
            elif category == "type":
                if all_type or tag_name in user_tags:
                    score += CATEGORY_WEIGHTS["type"]

        if valid:
            scored_practices.append((p, score))

    scored_practices.sort(key=lambda x: x[1], reverse=True)
    conn.close()

    return [
        {
            #"id": p["id"],
            #"title": p["title"],
            "youtube_url": p["youtube_url"],
            #"duration_minutes": p["duration_minutes"],
            #"score": round(score, 2)
        }
        for p, score in scored_practices[:top_n]
    ]

if __name__ == "__main__":
    # print do testowania outputu z poziomu konsoli

    """user_input = "low;blocks;back;relaxation;20minus;intermediate;yin;evening"
    results = find_best_matches(user_input)
    print("TOP DOPASOWANIA:\n")
    for i, r in enumerate(results, 1):
        print(f"{i}. {r['title']} ({r['duration_minutes']} min) - wynik: {r['score']}")
        print(f"   {r['youtube_url']}\n")"""

    # zwracanie wyniku (do użycia w aplikacji)
    try:
        if len(sys.argv) > 1 and sys.argv[1].strip():
            user_input = sys.argv[1]
        else:
            print(json.dumps({"error": "Invalid user input (user_input)."}, ensure_ascii=False))
            sys.exit(1)

        results = find_best_matches(user_input)

        # zwracamy wynik jako JSON
        print(json.dumps(results, ensure_ascii=False))

    except Exception as e:
        print(json.dumps({"error": f"Error: {str(e)}"}, ensure_ascii=False))
        sys.exit(1)