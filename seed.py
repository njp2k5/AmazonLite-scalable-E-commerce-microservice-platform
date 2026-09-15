import psycopg2
from datetime import datetime

import os
# DB Connection info
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

def seed():
    conn = get_connection()
    cursor = conn.cursor()
    
    print("Truncating Books table...")
    cursor.execute("TRUNCATE TABLE Books RESTART IDENTITY CASCADE;")
    
    secondary_images = [
        "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800",
        "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?auto=format&fit=crop&q=80&w=800",
        "https://images.unsplash.com/photo-1535905557558-afc4877a26fc?auto=format&fit=crop&q=80&w=800"
    ]

    books = [
        ("The Great Gatsby", "A story of the wealthy Jay Gatsby and his love for the beautiful Daisy Buchanan.", 10.99, 100, "fiction", "https://m.media-amazon.com/images/I/81af+MCATTL._AC_UF1000,1000_QL80_.jpg", "9780743273565", "F. Scott Fitzgerald", "Scribner", "Paperback", 180, "English", secondary_images),
        ("1984", "Among the seminal texts of the 20th century, Nineteen Eighty-Four is a rare work that grows more haunting as its futuristic purgatory becomes more real.", 9.99, 150, "fiction", "https://m.media-amazon.com/images/I/71N1o35mIEL._AC_UF1000,1000_QL80_.jpg", "9780451524935", "George Orwell", "Signet Classic", "Paperback", 328, "English", secondary_images),
        ("To Kill a Mockingbird", "The unforgettable novel of a childhood in a sleepy Southern town and the crisis of conscience that rocked it.", 12.99, 120, "fiction", "https://m.media-amazon.com/images/I/81gepf1eMqL._AC_UF1000,1000_QL80_.jpg", "9780060935467", "Harper Lee", "Harper Perennial", "Paperback", 336, "English", secondary_images),
        ("A Brief History of Time", "Stephen Hawking's classic work explains complex concepts of cosmology in an accessible way.", 14.99, 80, "science", "https://m.media-amazon.com/images/I/81nZ-929IEL._AC_UF1000,1000_QL80_.jpg", "9780553380163", "Stephen Hawking", "Bantam", "Paperback", 212, "English", secondary_images),
        ("The Hobbit", "A great modern classic and the prelude to The Lord of the Rings.", 11.99, 200, "fiction", "https://m.media-amazon.com/images/I/710+HcoP38L._AC_UF1000,1000_QL80_.jpg", "9780547928227", "J.R.R. Tolkien", "Houghton Mifflin Harcourt", "Paperback", 300, "English", secondary_images),
        ("Pride and Prejudice", "Few have failed to be charmed by the witty and independent spirit of Elizabeth Bennet in Austen's beloved classic.", 8.99, 90, "romance", "https://m.media-amazon.com/images/I/71Q1tPupKjL._AC_UF1000,1000_QL80_.jpg", "9780141439518", "Jane Austen", "Penguin Classics", "Paperback", 432, "English", secondary_images),
        ("The Catcher in the Rye", "The hero-narrator of The Catcher in the Rye is an ancient child of sixteen, a native New Yorker named Holden Caulfield.", 10.50, 110, "fiction", "https://m.media-amazon.com/images/I/81OthjkJBuL._AC_UF1000,1000_QL80_.jpg", "9780316769488", "J.D. Salinger", "Little, Brown and Company", "Paperback", 277, "English", secondary_images),
        ("Sapiens", "A Brief History of Humankind explores how biology and history have defined us and enhanced our understanding of what it means to be 'human'.", 19.99, 75, "history", "https://m.media-amazon.com/images/I/713jIoMO3UL._AC_UF1000,1000_QL80_.jpg", "9780062316097", "Yuval Noah Harari", "Harper", "Paperback", 464, "English", secondary_images),
        ("The Alchemist", "Paulo Coelho's enchanting novel has inspired a devoted following around the world.", 13.50, 130, "fiction", "https://m.media-amazon.com/images/I/71aFt4+OTOL._AC_UF1000,1000_QL80_.jpg", "9780062315007", "Paulo Coelho", "HarperOne", "Paperback", 197, "English", secondary_images),
        ("Atomic Habits", "No matter your goals, Atomic Habits offers a proven framework for improving--every day.", 16.99, 250, "self-help", "https://m.media-amazon.com/images/I/81YkqyaFVEL._AC_UF1000,1000_QL80_.jpg", "9780735211292", "James Clear", "Avery", "Hardcover", 320, "English", secondary_images),
        ("Dune", "Set on the desert planet Arrakis, Dune is the story of the boy Paul Atreides, heir to a noble family tasked with ruling an inhospitable world where the only thing of value is the 'spice' melange.", 15.50, 200, "science fiction", "https://m.media-amazon.com/images/I/81ym36dMVAL._AC_UF1000,1000_QL80_.jpg", "9780441172719", "Frank Herbert", "Ace Books", "Paperback", 412, "English", secondary_images),
        ("The Martian", "Six days ago, astronaut Mark Watney became one of the first people to walk on Mars. Now, he's sure he'll be the first person to die there.", 14.00, 180, "science fiction", "https://m.media-amazon.com/images/I/81z4kE4DveL._AC_UF1000,1000_QL80_.jpg", "9780553418026", "Andy Weir", "Crown Publishing", "Hardcover", 369, "English", secondary_images),
        ("Educated", "A memoir about a young girl who, kept out of school, leaves her survivalist family and goes on to earn a PhD from Cambridge University.", 16.00, 150, "biography", "https://m.media-amazon.com/images/I/71-0iE-t9qL._AC_UF1000,1000_QL80_.jpg", "9780399590504", "Tara Westover", "Random House", "Hardcover", 352, "English", secondary_images),
        ("The Midnight Library", "Between life and death there is a library, and within that library, the shelves go on forever. Every book provides a chance to try another life you could have lived.", 18.00, 220, "fiction", "https://m.media-amazon.com/images/I/81J6ZQ8A-bL._AC_UF1000,1000_QL80_.jpg", "9780525559474", "Matt Haig", "Viking", "Hardcover", 304, "English", secondary_images),
        ("Project Hail Mary", "Ryland Grace is the sole survivor on a desperate, last-chance mission—and if he fails, humanity and the earth itself will perish.", 17.50, 190, "science fiction", "https://m.media-amazon.com/images/I/81PzHjI21TL._AC_UF1000,1000_QL80_.jpg", "9780593135204", "Andy Weir", "Ballantine Books", "Hardcover", 496, "English", secondary_images),
    ]
    
    print("Inserting books...")
    for b in books:
        is_featured = b[0] == "The Midnight Library"
        is_bestseller = b[0] in ["The Great Gatsby", "1984", "Sapiens", "Atomic Habits", "Dune", "The Alchemist", "To Kill a Mockingbird", "Pride and Prejudice"]
        cursor.execute('''
            INSERT INTO Books (name, description, price, stock, category, is_bestseller, is_featured, image_url, isbn, author, publisher, format, pages, language, created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) RETURNING id
        ''', (b[0], b[1], b[2], b[3], b[4], is_bestseller, is_featured, b[5], b[6], b[7], b[8], b[9], b[10], b[11], datetime.now(), datetime.now()))
        
        book_id = cursor.fetchone()[0]
        for img in b[12]:
            cursor.execute('''
                INSERT INTO book_images (book_id, image_url)
                VALUES (%s, %s)
            ''', (book_id, img))
        
    conn.commit()
    cursor.close()
    conn.close()
    print("Done!")

if __name__ == '__main__':
    seed()
