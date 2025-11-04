import sqlite3
import json
import sys
from recommend_practice_based_on_answers import CATEGORY_WEIGHTS

DB_FILE = "yogo_database_git"


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


def recommend_for_you(time_of_day):
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
            ORDER BY RANDOM()
            LIMIT 10
        """, (time_of_day,))
        urls = [row[0] for row in cur.fetchall()]
        conn.close()
        return urls

    history_tags = []
    for pid in history_ids:
        history_tags.append(get_practice_tags(cur, pid))

    dominant_tags = {}
    for category in CATEGORY_WEIGHTS.keys():
        tags_in_cat = [tags[category] for tags in history_tags if category in tags]
        if tags_in_cat:
            dominant_tag = max(set(tags_in_cat), key=tags_in_cat.count)
            dominant_tags[category] = dominant_tag

    practices = get_all_practices(cur)

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
    conn.close()

    return [p["youtube_url"] for p, score in scored[:10]]


if __name__ == "__main__":
    try:
        if len(sys.argv) > 1 and sys.argv[1].strip():
            time_of_day = sys.argv[1].strip().lower()
        else:
            print(json.dumps({"error": "Missing argument: time_of_day"}, ensure_ascii=False))
            sys.exit(1)

        results = recommend_for_you(time_of_day)
        print(json.dumps(results, ensure_ascii=False))

    except Exception as e:
        print(json.dumps({"error": f"Error: {str(e)}"}, ensure_ascii=False))
        sys.exit(1)
