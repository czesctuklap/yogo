import sqlite3

DB_FILE = "yogo_database_git"

def set_db_path(path):
    global DB_FILE
    DB_FILE = path

CATEGORY_WEIGHTS = {
    "time_of_day": 2.5,
    "intensity": 5.0,
    "level": 5.0,
    "focus_area": 10.0,
    "type": 10.0,
    "goal": 10.0,
    "props": 5.0
}


def get_practice_tags(cur, practice_id):
    cur.execute("""
        SELECT t.category, t.name
        FROM tag t
        JOIN practice_tag pt ON t.id = pt.tag_id
        WHERE pt.practice_id = ?
    """, (practice_id,))
    return {category: name.lower() for category, name in cur.fetchall()}


def get_all_practices(cur):
    cur.execute("""
        SELECT p.id, p.youtube_url, t.category, t.name
        FROM practice p
        JOIN practice_tag pt ON p.id = pt.practice_id
        JOIN tag t ON t.id = pt.tag_id
    """)
    practices = {}
    for pid, url, category, tag_name in cur.fetchall():
        if pid not in practices:
            practices[pid] = {"id": pid, "youtube_url": url, "tags": {}}
        practices[pid]["tags"][category] = tag_name.lower()
    return practices


def recommend_for_you(time_of_day, top_n=10):
    conn = sqlite3.connect(DB_FILE)
    cur = conn.cursor()

    cur.execute("SELECT practice FROM history ORDER BY practiced_at DESC LIMIT 50")
    history_ids = [row[0] for row in cur.fetchall()]

    if not history_ids:
        cur.execute("""
            SELECT DISTINCT p.youtube_url
            FROM practice p
            JOIN practice_tag pt ON p.id = pt.practice_id
            JOIN tag t ON t.id = pt.tag_id
            WHERE t.category = 'time_of_day' AND t.name = ?
            ORDER BY RANDOM() LIMIT ?
        """, (time_of_day, top_n))

        urls = [row[0] for row in cur.fetchall()]
        conn.close()
        return urls

    history_tags = []
    for pid in history_ids:
        history_tags.append(get_practice_tags(cur, pid))

    dominant_tags = {}
    for category in CATEGORY_WEIGHTS.keys():
        tags_in_category = [tags[category] for tags in history_tags if category in tags]
        if tags_in_category:
            most_common = max(set(tags_in_category), key=tags_in_category.count)
            dominant_tags[category] = most_common

    practices = get_all_practices(cur)

    conn.close()

    scored = []
    for p in practices.values():
        if p["id"] in history_ids:
            continue

        score = 0
        for category, tag_name in p["tags"].items():
            if category in dominant_tags and tag_name == dominant_tags[category]:
                score += CATEGORY_WEIGHTS.get(category, 1.0)

        scored.append((p, score))

    scored.sort(key=lambda x: x[1], reverse=True)

    return [p["youtube_url"] for p, score in scored[:top_n]]
