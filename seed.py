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
        ("To Kill a Mockingbird", "The unforgettable novel of a childhood in a sleepy Southern town and the crisis of conscience that rocked it.", 12.99, 120, "fiction", "https://m.media-amazon.com/images/I/81gepf1eMqL._AC_UF1000,1000_QL80_.jpg", "9780060935467", "Harper Lee", "Harper Perennial", "Paperback", 336, "English", secondary_images),
        ("A Brief History of Time", "Stephen Hawking's classic work explains complex concepts of cosmology in an accessible way.", 14.99, 80, "science", "https://m.media-amazon.com/images/I/81nZ-929IEL._AC_UF1000,1000_QL80_.jpg", "9780553380163", "Stephen Hawking", "Bantam", "Paperback", 212, "English", secondary_images),
        ("The Hobbit", "A great modern classic and the prelude to The Lord of the Rings.", 11.99, 200, "fiction", "https://m.media-amazon.com/images/I/710+HcoP38L._AC_UF1000,1000_QL80_.jpg", "9780547928227", "J.R.R. Tolkien", "Houghton Mifflin Harcourt", "Paperback", 300, "English", secondary_images),
        ("Pride and Prejudice", "Few have failed to be charmed by the witty and independent spirit of Elizabeth Bennet in Austen's beloved classic.", 8.99, 90, "romance", "https://m.media-amazon.com/images/I/71Q1tPupKjL._AC_UF1000,1000_QL80_.jpg", "9780141439518", "Jane Austen", "Penguin Classics", "Paperback", 432, "English", secondary_images),
        ("The Catcher in the Rye", "The hero-narrator of The Catcher in the Rye is an ancient child of sixteen, a native New Yorker named Holden Caulfield.", 10.50, 110, "fiction", "https://m.media-amazon.com/images/I/81OthjkJBuL._AC_UF1000,1000_QL80_.jpg", "9780316769488", "J.D. Salinger", "Little, Brown and Company", "Paperback", 277, "English", secondary_images),
        ("Sapiens", "A Brief History of Humankind explores how biology and history have defined us and enhanced our understanding of what it means to be 'human'.", 19.99, 75, "history", "https://m.media-amazon.com/images/I/713jIoMO3UL._AC_UF1000,1000_QL80_.jpg", "9780062316097", "Yuval Noah Harari", "Harper", "Paperback", 464, "English", secondary_images),
        ("The Alchemist", "Paulo Coelho's enchanting novel has inspired a devoted following around the world.", 13.50, 130, "fiction", "https://m.media-amazon.com/images/I/71aFt4+OTOL._AC_UF1000,1000_QL80_.jpg", "9780062315007", "Paulo Coelho", "HarperOne", "Paperback", 197, "English", secondary_images),
        ("Atomic Habits", "No matter your goals, Atomic Habits offers a proven framework for improving--every day.", 16.99, 250, "self-help", "https://m.media-amazon.com/images/I/81YkqyaFVEL._AC_UF1000,1000_QL80_.jpg", "9780735211292", "James Clear", "Avery", "Hardcover", 320, "English", secondary_images),
        ("The Martian", "Six days ago, astronaut Mark Watney became one of the first people to walk on Mars. Now, he's sure he'll be the first person to die there.", 14.00, 180, "science fiction", "https://m.media-amazon.com/images/I/81z4kE4DveL._AC_UF1000,1000_QL80_.jpg", "9780553418026", "Andy Weir", "Crown Publishing", "Hardcover", 369, "English", secondary_images),
        ("Educated", "A memoir about a young girl who, kept out of school, leaves her survivalist family and goes on to earn a PhD from Cambridge University.", 16.00, 150, "biography", "https://m.media-amazon.com/images/I/71-0iE-t9qL._AC_UF1000,1000_QL80_.jpg", "9780399590504", "Tara Westover", "Random House", "Hardcover", 352, "English", secondary_images),
        ("Project Hail Mary", "Ryland Grace is the sole survivor on a desperate, last-chance mission—and if he fails, humanity and the earth itself will perish.", 17.50, 190, "science fiction", "https://m.media-amazon.com/images/I/81PzHjI21TL._AC_UF1000,1000_QL80_.jpg", "9780593135204", "Andy Weir", "Ballantine Books", "Hardcover", 496, "English", secondary_images),
        ("To the Lighthouse", "Virginia Woolf's masterpiece of modernist literature.", 12.5, 61, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=0", "9780183281708", "Virginia Woolf", "Classic Books", "Paperback", 199, "English", secondary_images),
        ("Moby-Dick", "The epic tale of Captain Ahab's obsessive quest.", 14.2, 63, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=1", "9780535589513", "Herman Melville", "Classic Books", "Paperback", 755, "English", secondary_images),
        ("War and Peace", "A broad panorama of Russian life during the Napoleonic era.", 25.0, 36, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=2", "9780930041016", "Leo Tolstoy", "Classic Books", "Paperback", 664, "English", secondary_images),
        ("The Odyssey", "Homer's epic poem of Odysseus's journey home.", 10.99, 146, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=3", "9780851506091", "Homer", "Classic Books", "Paperback", 573, "English", secondary_images),
        ("The Brothers Karamazov", "A passionate philosophical novel by Dostoevsky.", 18.5, 148, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=4", "9780677147319", "Fyodor Dostoevsky", "Classic Books", "Paperback", 227, "English", secondary_images),
        ("Crime and Punishment", "A novel about the mental anguish of Rodion Raskolnikov.", 15.0, 145, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=5", "9780220667373", "Fyodor Dostoevsky", "Classic Books", "Paperback", 373, "English", secondary_images),
        ("Madame Bovary", "Gustave Flaubert's story of Emma Bovary.", 11.2, 103, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=6", "9780650477471", "Gustave Flaubert", "Classic Books", "Paperback", 696, "English", secondary_images),
        ("The Divine Comedy", "Dante's journey through Hell, Purgatory, and Paradise.", 19.99, 162, "poetry", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=7", "9780544589337", "Dante Alighieri", "Classic Books", "Paperback", 359, "English", secondary_images),
        ("The Iliad", "Homer's epic about the Trojan War.", 11.5, 171, "poetry", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=8", "9780123427254", "Homer", "Classic Books", "Paperback", 591, "English", secondary_images),
        ("Don Quixote", "Miguel de Cervantes's classic tale of a delusional knight.", 16.75, 132, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=9", "9780204056218", "Miguel de Cervantes", "Classic Books", "Paperback", 474, "English", secondary_images),
        ("One Hundred Years of Solitude", "Gabriel García Márquez's magical realist epic.", 14.5, 92, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=10", "9780781833415", "Gabriel Garcia Marquez", "Classic Books", "Paperback", 391, "English", secondary_images),
        ("The Sound and the Fury", "William Faulkner's modernist novel.", 13.0, 25, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=11", "9780189468668", "William Faulkner", "Classic Books", "Paperback", 175, "English", secondary_images),
        ("Catch-22", "Joseph Heller's satirical war novel.", 12.99, 161, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=12", "9780884797566", "Joseph Heller", "Classic Books", "Paperback", 293, "English", secondary_images),
        ("Beloved", "Toni Morrison's novel about a former slave.", 14.0, 159, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=13", "9780804321664", "Toni Morrison", "Classic Books", "Paperback", 423, "English", secondary_images),
        ("Jane Eyre", "Charlotte Brontë's novel of romance and independence.", 9.5, 139, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=14", "9780958152477", "Charlotte Bronte", "Classic Books", "Paperback", 261, "English", secondary_images),
        ("Wuthering Heights", "Emily Brontë's tale of doomed love.", 8.99, 27, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=15", "9780286745879", "Emily Bronte", "Classic Books", "Paperback", 389, "English", secondary_images),
        ("Great Expectations", "Charles Dickens's coming-of-age story.", 10.0, 101, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=16", "9780813793221", "Charles Dickens", "Classic Books", "Paperback", 219, "English", secondary_images),
        ("The Grapes of Wrath", "John Steinbeck's novel of the Great Depression.", 13.5, 112, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=17", "9780440856165", "John Steinbeck", "Classic Books", "Paperback", 559, "English", secondary_images),
        ("Invisible Man", "Ralph Ellison's novel about an unnamed African American protagonist.", 15.5, 65, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=18", "9780555305254", "Ralph Ellison", "Classic Books", "Paperback", 359, "English", secondary_images),
        ("Ulysses", "James Joyce's modernist masterpiece.", 18.0, 140, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=19", "9780757754978", "James Joyce", "Classic Books", "Paperback", 193, "English", secondary_images),
        ("Frankenstein", "Mary Shelley's classic Gothic novel.", 8.5, 119, "science fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=20", "9780698545311", "Mary Shelley", "Classic Books", "Paperback", 629, "English", secondary_images),
        ("Dracula", "Bram Stoker's seminal vampire novel.", 9.0, 108, "horror", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=21", "9780938871226", "Bram Stoker", "Classic Books", "Paperback", 694, "English", secondary_images),
        ("The Picture of Dorian Gray", "Oscar Wilde's philosophical novel.", 10.5, 167, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=22", "9780454193554", "Oscar Wilde", "Classic Books", "Paperback", 448, "English", secondary_images),
        ("Brave New World", "Aldous Huxley's dystopian classic.", 12.0, 87, "science fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=23", "9780881869612", "Aldous Huxley", "Classic Books", "Paperback", 342, "English", secondary_images),
        ("Fahrenheit 451", "Ray Bradbury's novel about a future without books.", 11.5, 79, "science fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=24", "9780856407691", "Ray Bradbury", "Classic Books", "Paperback", 189, "English", secondary_images),
        ("The Lord of the Rings", "J.R.R. Tolkien's high fantasy epic.", 29.99, 199, "fantasy", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=25", "9780586289658", "J.R.R. Tolkien", "Classic Books", "Paperback", 403, "English", secondary_images),
        ("Harry Potter and the Sorcerer's Stone", "J.K. Rowling's magical tale.", 10.99, 112, "fantasy", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=26", "9780371662252", "J.K. Rowling", "Classic Books", "Paperback", 221, "English", secondary_images),
        ("The Chronicles of Narnia", "C.S. Lewis's beloved fantasy series.", 25.0, 10, "fantasy", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=27", "9780969958919", "C.S. Lewis", "Classic Books", "Paperback", 664, "English", secondary_images),
        ("Alice's Adventures in Wonderland", "Lewis Carroll's surreal children's book.", 7.99, 29, "fantasy", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=28", "9780506074800", "Lewis Carroll", "Classic Books", "Paperback", 200, "English", secondary_images),
        ("The Little Prince", "Antoine de Saint-Exupéry's philosophical novella.", 8.99, 50, "children", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=29", "9780185069377", "Antoine de Saint-Exupery", "Classic Books", "Paperback", 621, "English", secondary_images),
        ("Slaughterhouse-Five", "Kurt Vonnegut's anti-war novel.", 13.99, 133, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=30", "9780992381814", "Kurt Vonnegut", "Classic Books", "Paperback", 769, "English", secondary_images),
        ("The Handmaid's Tale", "Margaret Atwood's dystopian novel.", 14.99, 93, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=31", "9780747236398", "Margaret Atwood", "Classic Books", "Paperback", 608, "English", secondary_images),
        ("The Bell Jar", "Sylvia Plath's semi-autobiographical novel.", 11.99, 62, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=32", "9780616147186", "Sylvia Plath", "Classic Books", "Paperback", 737, "English", secondary_images),
        ("The Stranger", "Albert Camus's existentialist novel.", 9.99, 107, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=34", "9780151938019", "Albert Camus", "Classic Books", "Paperback", 624, "English", secondary_images),
        ("A Tale of Two Cities", "Charles Dickens's historical novel.", 8.99, 40, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=35", "9780666704858", "Charles Dickens", "Classic Books", "Paperback", 721, "English", secondary_images),
        ("Les Misérables", "Victor Hugo's epic historical novel.", 19.99, 91, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=36", "9780488428406", "Victor Hugo", "Classic Books", "Paperback", 430, "English", secondary_images),
        ("Anna Karenina", "Leo Tolstoy's tragic romance.", 16.5, 69, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=37", "9780334199007", "Leo Tolstoy", "Classic Books", "Paperback", 766, "English", secondary_images),
        ("Middlemarch", "George Eliot's study of provincial life.", 14.5, 172, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=39", "9780189196358", "George Eliot", "Classic Books", "Paperback", 181, "English", secondary_images),
        ("In Search of Lost Time", "Marcel Proust's monumental work.", 35.0, 36, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=40", "9780212701496", "Marcel Proust", "Classic Books", "Paperback", 405, "English", secondary_images),
        ("The Trial", "Franz Kafka's surreal bureaucratic nightmare.", 10.5, 149, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=41", "9780894044835", "Franz Kafka", "Classic Books", "Paperback", 429, "English", secondary_images),
        ("The Metamorphosis", "Franz Kafka's novella of transformation.", 7.5, 120, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=42", "9780226218272", "Franz Kafka", "Classic Books", "Paperback", 476, "English", secondary_images),
        ("Heart of Darkness", "Joseph Conrad's novella of colonialism.", 8.5, 192, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=43", "9780890466365", "Joseph Conrad", "Classic Books", "Paperback", 739, "English", secondary_images),
        ("Gulliver's Travels", "Jonathan Swift's satirical travelogue.", 9.5, 83, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=44", "9780428958072", "Jonathan Swift", "Classic Books", "Paperback", 349, "English", secondary_images),
        ("Robinson Crusoe", "Daniel Defoe's castaway novel.", 8.5, 126, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=45", "9780247449797", "Daniel Defoe", "Classic Books", "Paperback", 623, "English", secondary_images),
        ("The Count of Monte Cristo", "Alexandre Dumas's adventure novel.", 18.5, 127, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=46", "9780185531641", "Alexandre Dumas", "Classic Books", "Paperback", 512, "English", secondary_images),
        ("The Canterbury Tales", "Geoffrey Chaucer's collection of stories.", 12.5, 161, "poetry", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=48", "9780655591925", "Geoffrey Chaucer", "Classic Books", "Paperback", 389, "English", secondary_images),
        ("Leaves of Grass", "Walt Whitman's poetry collection.", 11.5, 64, "poetry", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=49", "9780747664701", "Walt Whitman", "Classic Books", "Paperback", 542, "English", secondary_images),
        ("The Sun Also Rises", "Ernest Hemingway's novel of the Lost Generation.", 12.99, 179, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=50", "9780568941348", "Ernest Hemingway", "Classic Books", "Paperback", 624, "English", secondary_images),
        ("On the Road", "Jack Kerouac's Beat Generation classic.", 13.5, 51, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=51", "9780820394577", "Jack Kerouac", "Classic Books", "Paperback", 736, "English", secondary_images),
        ("The Old Man and the Sea", "Ernest Hemingway's novella.", 9.99, 149, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=52", "9780433240197", "Ernest Hemingway", "Classic Books", "Paperback", 364, "English", secondary_images),
        ("The Call of the Wild", "Jack London's adventure novel.", 8.99, 84, "fiction", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=53", "9780538937031", "Jack London", "Classic Books", "Paperback", 779, "English", secondary_images),
        ("The Secret Garden", "Frances Hodgson Burnett's children's novel.", 7.99, 175, "children", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random=54", "9780416114056", "Frances Hodgson Burnett", "Classic Books", "Paperback", 798, "English", secondary_images),
    ]
    
    print("Inserting books...")
    for b in books:
        is_featured = b[0] == "The Odyssey"
        is_bestseller = b[0] in ["The Great Gatsby", "The Odyssey", "Moby-Dick", "Sapiens", "Atomic Habits", "War and Peace", "The Alchemist", "To Kill a Mockingbird", "Pride and Prejudice"]
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
