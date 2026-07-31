import psycopg2
from datetime import datetime

# DB Connection info
db_host = "ep-crimson-hat-appwfmvz-pooler.c-7.us-east-1.aws.neon.tech"
db_port = "5432"
db_name = "neondb"
db_user = "neondb_owner"
db_password = "npg_Chu10NovqHGi"

def get_connection():
    return psycopg2.connect(
        host=db_host,
        port=db_port,
        dbname=db_name,
        user=db_user,
        password=db_password,
        sslmode='require'
    )

def seed():
    conn = get_connection()
    cursor = conn.cursor()
    
    print("Truncating Books table...")
    cursor.execute("TRUNCATE TABLE Books RESTART IDENTITY CASCADE;")
    
    books = [
        ("The Great Gatsby", "A story of the wealthy Jay Gatsby and his love for the beautiful Daisy Buchanan.", 10.99, 100, "fiction", "https://m.media-amazon.com/images/I/81af+MCATTL._AC_UF1000,1000_QL80_.jpg", "9780743273565", "F. Scott Fitzgerald", "Scribner", "Paperback", 180, "English"),
        ("1984", "Among the seminal texts of the 20th century, Nineteen Eighty-Four is a rare work that grows more haunting as its futuristic purgatory becomes more real.", 9.99, 150, "fiction", "https://m.media-amazon.com/images/I/71N1o35mIEL._AC_UF1000,1000_QL80_.jpg", "9780451524935", "George Orwell", "Signet Classic", "Paperback", 328, "English"),
        ("To Kill a Mockingbird", "The unforgettable novel of a childhood in a sleepy Southern town and the crisis of conscience that rocked it.", 12.99, 120, "fiction", "https://m.media-amazon.com/images/I/81gepf1eMqL._AC_UF1000,1000_QL80_.jpg", "9780060935467", "Harper Lee", "Harper Perennial", "Paperback", 336, "English"),
        ("A Brief History of Time", "Stephen Hawking's classic work explains complex concepts of cosmology in an accessible way.", 14.99, 80, "science", "https://m.media-amazon.com/images/I/81nZ-929IEL._AC_UF1000,1000_QL80_.jpg", "9780553380163", "Stephen Hawking", "Bantam", "Paperback", 212, "English"),
        ("The Hobbit", "A great modern classic and the prelude to The Lord of the Rings.", 11.99, 200, "fiction", "https://m.media-amazon.com/images/I/710+HcoP38L._AC_UF1000,1000_QL80_.jpg", "9780547928227", "J.R.R. Tolkien", "Houghton Mifflin Harcourt", "Paperback", 300, "English"),
        ("Pride and Prejudice", "Few have failed to be charmed by the witty and independent spirit of Elizabeth Bennet in Austen's beloved classic.", 8.99, 90, "romance", "https://m.media-amazon.com/images/I/71Q1tPupKjL._AC_UF1000,1000_QL80_.jpg", "9780141439518", "Jane Austen", "Penguin Classics", "Paperback", 432, "English"),
        ("The Catcher in the Rye", "The hero-narrator of The Catcher in the Rye is an ancient child of sixteen, a native New Yorker named Holden Caulfield.", 10.50, 110, "fiction", "https://m.media-amazon.com/images/I/81OthjkJBuL._AC_UF1000,1000_QL80_.jpg", "9780316769488", "J.D. Salinger", "Little, Brown and Company", "Paperback", 277, "English"),
        ("Sapiens", "A Brief History of Humankind explores how biology and history have defined us and enhanced our understanding of what it means to be 'human'.", 19.99, 75, "history", "https://m.media-amazon.com/images/I/713jIoMO3UL._AC_UF1000,1000_QL80_.jpg", "9780062316097", "Yuval Noah Harari", "Harper", "Paperback", 464, "English"),
        ("The Alchemist", "Paulo Coelho's enchanting novel has inspired a devoted following around the world.", 13.50, 130, "fiction", "https://m.media-amazon.com/images/I/71aFt4+OTOL._AC_UF1000,1000_QL80_.jpg", "9780062315007", "Paulo Coelho", "HarperOne", "Paperback", 197, "English"),
        ("Atomic Habits", "No matter your goals, Atomic Habits offers a proven framework for improving--every day.", 16.99, 250, "self-help", "https://m.media-amazon.com/images/I/81YkqyaFVEL._AC_UF1000,1000_QL80_.jpg", "9780735211292", "James Clear", "Avery", "Hardcover", 320, "English"),
    ]
    
    print("Inserting books...")
    for b in books:
        cursor.execute('''
            INSERT INTO Books (name, description, price, stock, category, image_url, isbn, author, publisher, format, pages, language, created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        ''', (b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7], b[8], b[9], b[10], b[11], datetime.now(), datetime.now()))
        
    conn.commit()
    cursor.close()
    conn.close()
    print("Done!")

if __name__ == '__main__':
    seed()
