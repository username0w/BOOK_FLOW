package com.clover.bookflow.domain.blogpost.entity;

import com.clover.bookflow.domain.member.entity.Member;

public class BlogPostTestHelper {

  public static BlogPost createDraftPost(Member author) {
    return BlogPost.createDraft("Draft Title", "Draft Content", author);
  }

  public static BlogPost createPublishedPost(Member author) {
    return BlogPost.createPublished("Published Title", "Published Content", author);
  }

  public static BlogPost createDeletedPost(Member author) {
    BlogPost post = createDraftPost(author);
    post.delete();
    return post;
  }

}
