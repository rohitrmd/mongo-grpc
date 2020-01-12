package blog.server;

import com.mongodb.client.*;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.print.Doc;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:30011");
    private MongoDatabase database = mongoClient.getDatabase("blogDb");
    private MongoCollection<Document> collection = database.getCollection("blog");

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {
        Blog blog = request.getBlog();
        Document document = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        collection.insertOne(document);

        String id = document.getObjectId("_id").toString();

        CreateBlogResponse blogResponse = CreateBlogResponse.newBuilder()
                .setBlog(Blog.newBuilder(blog).setId(id).build())
                .build();

        responseObserver.onNext(blogResponse);
        responseObserver.onCompleted();

    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {
        String blogId = request.getId();
        Document doc = collection.find(eq("_id", new ObjectId(blogId))).first();
        if(doc == null) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Document you are looing for is not found.").asException());
        } else {
            Blog blog = Blog.newBuilder()
                    .setId(doc.get("_id").toString())
                    .setAuthorId(doc.getString("author_id"))
                    .setContent(doc.getString("content"))
                    .setTitle(doc.getString("title"))
                    .build();
            responseObserver.onNext(ReadBlogResponse.newBuilder().setBlog(blog).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {
        Blog requestBlog = request.getBlog();
        String blogId = requestBlog.getId();
        Document doc = collection.find(eq("_id", new ObjectId(blogId))).first();

        if(doc == null) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Document you are looing for is not found.").asException());
        } else {

            Document updatedDoc = new Document("author_id", requestBlog.getAuthorId())
                    .append("title", requestBlog.getTitle())
                    .append("content", requestBlog.getContent());


            collection.replaceOne(doc, updatedDoc);

            responseObserver.onNext(UpdateBlogResponse.newBuilder().setBlog(requestBlog).build());
            responseObserver.onCompleted();
        }

    }
}
