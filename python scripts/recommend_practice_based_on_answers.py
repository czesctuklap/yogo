import sqlite3

DB_FILE = "yogo_database_git"

CATEGORY_WEIGHTS = {
    "time_of_day": 0.25,
    "intensity": 0.5,
    "level": 0.5,
    "focus_area": 1.0,
    "type": 1.0,
    "goal": 1.0,
    "props": 0.5
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
            "User input must contain exactly 8 tags in order: intensity;props;focus_area;goal;duration;level;type;time_of_day"
        )

    user_intensity, user_props, user_focus, user_goal, duration_tag, user_level, user_type, user_time = user_tags

    all_focus = user_focus == "all"
    all_goal = user_goal == "all"
    all_type = user_type == "all"

    min_duration, max_duration = get_duration_range([duration_tag])

    cur.execute("""
        SELECT p.id, p.title, p.youtube_url, p.duration_minutes,
               t.name, t.category
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
        practices[pid]["tags"].append({
            "name": tag_name.lower(),
            "category": category
        })

    scored_practices = []

    for p in practices.values():
        score = 0
        valid = True

        if p["duration_minutes"] is not None and not (min_duration <= p["duration_minutes"] <= max_duration):
            continue

        for tag in p["tags"]:
            tag_name = tag["name"]
            category = tag["category"]

            if category == "time_of_day":
                if tag_name == user_time:
                    score += CATEGORY_WEIGHTS["time_of_day"]
                continue

            if category == "level":
                allowed = LEVEL_WEIGHTED[user_level]
                if tag_name not in allowed:
                    valid = False
                    break
                score += CATEGORY_WEIGHTS["level"] * allowed[tag_name]

            elif category == "props":
                if user_props == "none" and tag_name != "none":
                    valid = False
                    break
                elif user_props == "blocks" and tag_name not in ["blocks", "none"]:
                    valid = False
                    break
                elif user_props == "belt" and tag_name not in ["belt", "none"]:
                    valid = False
                    break
                elif user_props != "all" and tag_name == user_props:
                    score += CATEGORY_WEIGHTS["props"]
                elif user_props == "all":
                    pass

            elif category == "intensity":
                if user_intensity != "all":
                    allowed_intensity = INTENSITY_HIERARCHY[user_intensity]
                    if tag_name not in allowed_intensity:
                        valid = False
                        break
                    if tag_name == user_intensity:
                        score += CATEGORY_WEIGHTS["intensity"]

            elif category == "focus_area":
                if all_focus or tag_name == user_focus:
                    score += CATEGORY_WEIGHTS["focus_area"]

            elif category == "goal":
                if all_goal or tag_name == user_goal:
                    score += CATEGORY_WEIGHTS["goal"]

            elif category == "type":
                if all_type or tag_name == user_type:
                    score += CATEGORY_WEIGHTS["type"]

        if valid:
            scored_practices.append((p, score))

    conn.close()

    scored_practices.sort(key=lambda x: x[1], reverse=True)

    return [
        {"youtube_url": p["youtube_url"]}
        for p, score in scored_practices[:top_n]
    ]
