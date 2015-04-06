package course;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class BlogPostDAO {
    MongoCollection<Document> postsCollection;

    public BlogPostDAO(final MongoDatabase blogDatabase) {
        postsCollection = blogDatabase.getCollection("posts");
    }

    // Return a single post corresponding to a permalink
    public Document findByPermalink(String permalink) {

        // XXX HW 3.2,  Work Here
        BasicDBObject query = new BasicDBObject("permalink", permalink);      

        return postsCollection.find(query).first();
    }

    // Return a list of posts in descending order. Limit determines
    // how many posts are returned.
    public List<Document> findByDateDescending(int limit) {

        // XXX HW 3.2,  Work Here
        // Return a list of DBObjects, each one a post from the posts collection     
        
        FindIterable<Document> cursor = postsCollection.find().sort(new BasicDBObject("date",-1)).limit(limit);
        
        List<Document> posts = new ArrayList<Document>();
               
        Iterator i = cursor.iterator();
        
        while( i.hasNext() )
        {                    
            posts.add((Document) i.next());            
        }

        return posts;
    }


    public String addPost(String title, String body, List tags, String username) {

        System.out.println("inserting blog entry " + title + " " + body);

        String permalink = title.replaceAll("\\s", "_"); // whitespace becomes _
        permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
        permalink = permalink.toLowerCase();

        // XXX HW 3.2, Work Here
        // Remember that a valid post has the following keys:
        // author, body, permalink, tags, comments, date
        //
        // A few hints:
        // - Don't forget to create an empty list of comments
        // - for the value of the date key, today's datetime is fine.
        // - tags are already in list form that implements suitable interface.
        // - we created the permalink for you above.   
        
        List<BasicDBObject> comments = new ArrayList<>();

        // Build the post object and insert it
        Document post = new Document("title", title).
                              append("author", username).
                              append("body", body).
                              append("permalink", permalink).
                              append("tags", tags).
                              append("comments", comments).
                              append("date", new Date());
                
        postsCollection.insertOne(post);

        return permalink;
    }




    // White space to protect the innocent








    // Append a comment to a blog post
    public void addPostComment(final String name, final String email, final String body,
                               final String permalink) {

        // XXX HW 3.3, Work Here
        // Hints:
        // - email is optional and may come in NULL. Check for that.
        // - best solution uses an update command to the database and a suitable
        //   operator to append the comment on to any existing list of comments
        Document post = findByPermalink(permalink);
        
        List<BasicDBObject> comments = (List<BasicDBObject>) post.get("comments");
        
        String emailTemp = email;
        
        if (email == null)
        {
            emailTemp = "none";
        }        
        
        comments.add(new BasicDBObject("author",name).append("body", body).append("email", emailTemp));
        
        BasicDBObject newDocument = new BasicDBObject();
        newDocument.append("$set", new BasicDBObject().append("comments", comments));
     
        BasicDBObject searchQuery = new BasicDBObject().append("permalink", permalink);
     
        postsCollection.updateOne(searchQuery, newDocument);
    }
}
