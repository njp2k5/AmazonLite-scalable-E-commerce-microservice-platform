import psycopg2
import os

db_host = os.getenv("DB_HOST", "ep-crimson-hat-appwfmvz-pooler.c-7.us-east-1.aws.neon.tech")
db_port = os.getenv("DB_PORT", "5432")
db_name = os.getenv("DB_NAME", "neondb")
db_user = os.getenv("DB_USER", "neondb_owner")
db_password = os.getenv("DB_PASSWORD", "npg_Chu10NovqHGi")

def get_connection():
    return psycopg2.connect(
        f"postgresql://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}?sslmode=require"
    )

def check_db():
    try:
        conn = get_connection()
        cursor = conn.cursor()
        
        cursor.execute("SELECT name, is_bestseller, image_url FROM Books WHERE name IN ('1984', 'Dune', 'The Midnight Library', 'The Odyssey', 'Moby-Dick');")
        rows = cursor.fetchall()
        print("Books:")
        for row in rows:
            print(row)
            
        cursor.close()
        conn.close()
    except Exception as e:
        print("Error:", e)

if __name__ == '__main__':
    check_db()
