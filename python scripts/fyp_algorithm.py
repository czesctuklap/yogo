import sqlite3
import json
import sys
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from recommend_practice_based_on_answers import CATEGORY_WEIGHTS

DB_FILE = "yogo_database_git"


def get_all_practices(cur):
    cur.execute("""
        SELECT p.id, p.youtube_url, p.title, p.duration_minutes, t.category, t.name
        FROM practice p
        JOIN practice_tag pt ON p.id = pt.practice_id
        JOIN tag t ON t.id = pt.tag_id
    """)
    practices = {}
    tags_all = set()
    for pid, url, title, duration, category, tag_name in cur.fetchall():
        if pid not in practices:
            practices[pid] = {
                "id": pid,
                "youtube_url": url,
                "title": title,
                "duration_minutes": duration,
                "tags": {}
            }
        tag_name = tag_name.lower()
        practices[pid]["tags"][category] = tag_name
        tags_all.add((category, tag_name))
    return practices, tags_all


def recommend_for_you(time_of_day, top_n=10):
    conn = sqlite3.connect(DB_FILE)
    cur = conn.cursor()

    cur.execute("SELECT practice FROM history ORDER BY practiced_at DESC LIMIT 50")
    history_ids = [row[0] for row in cur.fetchall()]

    if not history_ids:
        cur.execute("""
            SELECT DISTINCT p.title, p.youtube_url, p.duration_minutes
            FROM practice p
            JOIN practice_tag pt ON p.id = pt.practice_id
            JOIN tag t ON t.id = pt.tag_id
            WHERE t.category = 'time_of_day' AND t.name = ?
            ORDER BY RANDOM() LIMIT ?
        """, (time_of_day, top_n))
        urls = [
            {
                #"title": row[0],
                "youtube_url": row[1],
                #"duration_minutes": row[2],
                #"score": 0.0
            }
            for row in cur.fetchall()
        ]
        conn.close()
        return urls

    practices, tags_all = get_all_practices(cur)
    conn.close()

    category_tags = {}
    for category, tag_name in tags_all:
        category_tags.setdefault(category, set()).add(tag_name)

    def encode_tags(tag_dict):
        vector = []
        for category, tag_set in category_tags.items():
            base_weight = CATEGORY_WEIGHTS.get(category, 1.0)
            for tag in sorted(tag_set):
                value = 1.0 if tag_dict.get(category) == tag else 0.0
                vector.append(value * base_weight)
        return np.array(vector, dtype=float)

    user_vectors = []
    for pid in history_ids:
        if pid in practices:
            user_vectors.append(encode_tags(practices[pid]["tags"]))

    if not user_vectors:
        return []

    user_profile = np.mean(user_vectors, axis=0)

    X = []
    P = []
    for p in practices.values():
        if p["id"] in history_ids:
            continue
        X.append(encode_tags(p["tags"]))
        P.append(p)

    if not X:
        return []

    X = np.array(X)

    sims = cosine_similarity([user_profile], X)[0]

    scored = [
        {
            #"title": p["title"],
            "youtube_url": p["youtube_url"],
            #"duration_minutes": p["duration_minutes"],
            #"score": round(float(sim), 4)
        }
        for p, sim in zip(P, sims)
    ]

    scored.sort(key=lambda x: x["score"], reverse=True)

    return scored[:top_n]


if __name__ == "__main__":
    try:
        if len(sys.argv) > 1 and sys.argv[1].strip():
            time_of_day = sys.argv[1].strip().lower()
        else:
            print(json.dumps({"error": "Missing argument: time_of_day"}, ensure_ascii=False))
            sys.exit(1)

        results = recommend_for_you("morning")
        print(json.dumps(results, ensure_ascii=False))

    except Exception as e:
        print(json.dumps({"error": f"Error: {str(e)}"}, ensure_ascii=False))
        sys.exit(1)
