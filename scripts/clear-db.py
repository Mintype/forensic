#!/usr/bin/env python3

import sqlite3
from pathlib import Path

db_path = Path(__file__).parent.parent / "run" / "config" / "forensic.db"

if not db_path.exists():
    print(f"Database not found: {db_path}")
    exit()

conn = sqlite3.connect(db_path)

cursor = conn.cursor()
cursor.execute("DELETE FROM logs;")

conn.commit()
conn.close()

print("Cleared all logs")