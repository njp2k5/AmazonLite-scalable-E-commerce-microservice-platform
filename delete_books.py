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

def delete_books():
    try:
        conn = get_connection()
        cursor = conn.cursor()
        
        books_to_delete = [
            "1984", "Dune", "The Midnight Library", "The Great Gatsby", 
            "To Kill a Mockingbird", "Sapiens", "Atomic Habits", "The Alchemist"
        ]
        
        print("Finding book IDs to delete...")
        cursor.execute("SELECT id FROM Books WHERE image_url IS NULL OR name = ANY(%s);", (books_to_delete,))
        book_ids = [row[0] for row in cursor.fetchall()]
        
        if not book_ids:
            print("No books found to delete.")
        else:
            print(f"Deleting {len(book_ids)} books from book_images...")
            cursor.execute("DELETE FROM book_images WHERE book_id = ANY(%s);", (book_ids,))
            
            print("Deleting books from Books table...")
            cursor.execute("DELETE FROM Books WHERE id = ANY(%s);", (book_ids,))
            
        conn.commit()
        cursor.close()
        conn.close()
        print("Done!")
    except Exception as e:
        print("Error:", e)

if __name__ == '__main__':
    delete_books()
