import psycopg2
import os

db_host = os.getenv("DB_HOST", "ep-crimson-hat-appwfmvz-pooler.c-7.us-east-1.aws.neon.tech")
db_port = os.getenv("DB_PORT", "5432")
db_name = os.getenv("DB_NAME", "neondb")
db_user = os.getenv("DB_USER", "neondb_owner")
db_password = os.getenv("DB_PASSWORD", "npg_Chu10NovqHGi")

def alter():
    conn = psycopg2.connect(
        host=db_host,
        port=db_port,
        dbname=db_name,
        user=db_user,
        password=db_password
    )
    cursor = conn.cursor()
    
    print("Creating book_images table...")
    try:
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS book_images (
                book_id BIGINT NOT NULL,
                image_url VARCHAR(500),
                CONSTRAINT fk_book_images_books FOREIGN KEY (book_id) REFERENCES Books(id) ON DELETE CASCADE
            );
        ''')
    except Exception as e:
        print(e)
        conn.rollback()

    conn.commit()
    cursor.close()
    conn.close()
    print("Done!")

if __name__ == '__main__':
    alter()
