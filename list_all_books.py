import psycopg2
import os

db_host = os.getenv("DB_HOST", "ep-crimson-hat-appwfmvz-pooler.c-7.us-east-1.aws.neon.tech")
db_port = os.getenv("DB_PORT", "5432")
db_name = os.getenv("DB_NAME", "neondb")
db_user = os.getenv("DB_USER", "neondb_owner")
db_password = os.getenv("DB_PASSWORD", "npg_Chu10NovqHGi")

conn = psycopg2.connect(
    f"postgresql://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}?sslmode=require"
)
cursor = conn.cursor()
cursor.execute("SELECT id, name, is_bestseller, is_featured, image_url FROM Books ORDER BY id;")
rows = cursor.fetchall()
print(f"Total: {len(rows)} books\n")
for r in rows:
    print(f"id={r[0]:3d}  bestseller={r[2]}  featured={r[3]}  name={r[1]}")
    print(f"        image={r[4]}")
cursor.close()
conn.close()
