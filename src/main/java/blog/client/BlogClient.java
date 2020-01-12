package blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {
    public static void main(String[] args) {
        BlogClient blogClient = new BlogClient();
        blogClient.runClient();
    }

    private void runClient() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50001)
                .usePlaintext()
                .build();

        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);
        Blog blog = Blog.newBuilder()
                .setAuthorId("1")
                .setTitle("Millionaire Next Door")
                .setContent("Seven steps to be next millionaire.")
                .build();

        CreateBlogResponse response = blogClient.createBlog(CreateBlogRequest.newBuilder().setBlog(blog).build());
        System.out.println(response.getBlog());

        String blogId = response.getBlog().getId();
        ReadBlogResponse readBlogResponse = blogClient
                .readBlog(ReadBlogRequest.newBuilder().setId(blogId).build());
        System.out.println("Read blog result = " + readBlogResponse);
    }
}
