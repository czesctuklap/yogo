import sqlite3
import pandas as pd

# konfiguracja
EXCEL_FILE = "yoga_videos_with_duration.xlsx"
DB_FILE = "yogo_database"

# wczytywanie danych z excela
df = pd.read_excel(EXCEL_FILE)

print("Kolumny wczytane z pliku:", df.columns.tolist())

# polaczenie z baza
conn = sqlite3.connect(DB_FILE)
cur = conn.cursor()

# pobieranie id tagu po nazwie
def get_tag_id(tag_name):
    cur.execute("SELECT id FROM tag WHERE name = ?", (str(tag_name).strip(),))
    result = cur.fetchone()
    return result[0] if result else None

# iteracja po wierszach
for _, row in df.iterrows():
    title = row["title"]
    youtube_url = row["link"]
    duration = int(row["duration_minutes"]) if not pd.isna(row["duration_minutes"]) else None

    # wstawianie rekordu do practice
    cur.execute("""
        INSERT INTO practice (title, youtube_url, duration_minutes)
        VALUES (?, ?, ?)
    """, (title, youtube_url, duration))

    practice_id = cur.lastrowid  # ID nowo dodanej praktyki

    # pola z tagami do przetworzenia
    tag_columns = ["author", "props", "time_of_day", "intensity", "level", "focus_area", "type", "goal"]

    for col in tag_columns:
        value = row[col]
        if pd.isna(value):
            continue

        # w przypadku gdy wartosci sa oddzielone , np. "morning, evening"
        tag_values = [v.strip() for v in str(value).split(",") if v.strip()]

        for tag_name in tag_values:
            tag_id = get_tag_id(tag_name)
            if tag_id:
                cur.execute("""
                    INSERT INTO practice_tag (practice_id, tag_id)
                    VALUES (?, ?)
                """, (practice_id, tag_id))
            else:
                print(f"Tag '{tag_name}' z kolumny '{col}' nie istnieje w tabeli 'tag'.")

# zapis do bazy
conn.commit()
conn.close()

print("Import zakończony pomyślnie.")
