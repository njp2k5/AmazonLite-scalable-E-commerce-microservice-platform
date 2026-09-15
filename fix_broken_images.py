import psycopg2
import os

db_host = os.getenv("DB_HOST", "127.0.0.1")
db_port = os.getenv("DB_PORT", "5432")
db_name = os.getenv("DB_NAME", "amazonlite")
db_user = os.getenv("DB_USER", "postgres")
db_password = os.getenv("DB_PASSWORD", "password")

def get_connection():
    return psycopg2.connect(
        host=db_host,
        port=db_port,
        dbname=db_name,
        user=db_user,
        password=db_password
    )

def fix_db():
    conn = get_connection()
    cursor = conn.cursor()
    
    print("Fixing broken image URLs in Books table...")
    # Set image_url to NULL for broken amazon links
    cursor.execute("UPDATE Books SET image_url = NULL WHERE image_url LIKE '%media-amazon.com%';")
    
    print("Deleting broken images from book_images table...")
    # Delete from book_images for broken amazon links
    cursor.execute("DELETE FROM book_images WHERE image_url LIKE '%media-amazon.com%';")
    
    print("Updating bestseller flags...")
    # Remove bestseller flag from books with missing images
    cursor.execute("UPDATE Books SET is_bestseller = false WHERE image_url IS NULL;")
    
    # Add bestseller flag to some classic books that have valid unsplash images
    bestseller_replacements = ["Moby-Dick", "The Odyssey", "War and Peace", "The Brothers Karamazov"]
    for title in bestseller_replacements:
        cursor.execute("UPDATE Books SET is_bestseller = true WHERE name = %s;", (title,))
        
    print("Updating featured flag...")
    # The Midnight Library is the featured book, let's keep it featured but its image is removed. 
    # Or maybe set another book as featured? Let's set 'The Odyssey' as featured instead if 'The Midnight Library' has no image.
    cursor.execute("UPDATE Books SET is_featured = false WHERE name = 'The Midnight Library';")
    cursor.execute("UPDATE Books SET is_featured = true WHERE name = 'The Odyssey';")
        
    conn.commit()
    cursor.close()
    conn.close()
    print("Done!")

if __name__ == '__main__':
    fix_db()
