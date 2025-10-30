import sqlite3
import json
import sys

DB_FILE = "yogo_database_git"

# wagi kategorii tagów
CATEGORY_WEIGHTS = {
    "time_of_day": 1.0,
    "intensity": 2.75,
    "level": 4.0,
    "focus_area": 3.0,
    "type": 13.5,
    "goal": 3.5,
    "props": 2.0
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
        return (20, 45)
    elif "45plus" in user_tags:
        return (45, 999)
    return (0, 999)  # brak ograniczenia

def find_best_matches(user_answers_str, top_n=10):
    conn = sqlite3.connect(DB_FILE)
    cur = conn.cursor()

    user_tags = [t.strip().lower() for t in user_answers_str.split(";") if t.strip()]

    cur.execute("SELECT id, name, category FROM tag")
    tag_data = cur.fetchall()
    tag_lookup = {name.lower(): {"id": tag_id, "category": category} for tag_id, name, category in tag_data}

    user_tag_info = [tag_lookup[t] for t in user_tags if t in tag_lookup]

    user_level = next((t for t in user_tags if t in LEVEL_WEIGHTED.keys()), None)
    user_props = next((t for t in user_tags if t in ["none", "blocks", "belt"]), None)
    user_intensity = next((t for t in user_tags if t in INTENSITY_HIERARCHY.keys()), None)
    min_duration, max_duration = get_duration_range(user_tags)

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

            if category == "time_of_day" and tag_name not in user_tags:
                continue

            # level
            if category == "level":
                if user_level:
                    allowed_levels = LEVEL_WEIGHTED[user_level]
                    if tag_name not in allowed_levels:
                        valid = False
                        break
                    else:
                        # dodaje wagę zależną od poziomu
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

                if tag_name == user_props:
                    score += CATEGORY_WEIGHTS["props"]

            # intensity
            elif category == "intensity":
                if user_intensity:
                    allowed_intensity = INTENSITY_HIERARCHY[user_intensity]
                    if tag_name not in allowed_intensity:
                        valid = False
                        break
                    if tag_name == user_intensity:
                        score += CATEGORY_WEIGHTS["intensity"]

            # inne kategorie
            elif tag_name in user_tags:
                score += CATEGORY_WEIGHTS.get(category, 1.0)

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

    #user_input = "high;blocks;back;strength;2045;intermediate;vinyasa;evening"
    #results = find_best_matches(user_input)
    #print("TOP DOPASOWANIA:\n")
    #for i, r in enumerate(results, 1):
        #print(f"{i}. {r['title']} ({r['duration_minutes']} min) - wynik: {r['score']}")
        #print(f"   {r['youtube_url']}\n")

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