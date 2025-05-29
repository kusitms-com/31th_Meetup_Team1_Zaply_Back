package org.zapply.product.domain.posting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.zapply.product.domain.project.entity.Project;
import org.zapply.product.global.BaseTimeEntity;
import org.zapply.product.domain.posting.enumerate.PostingState;
import org.zapply.product.global.clova.enuermerate.SNSType;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Posting extends BaseTimeEntity {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postingId;

    @Column
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column
    @Enumerated(EnumType.STRING)
    private SNSType postingType;

    @Column
    private String postingContent;

    @Column
    @Enumerated(EnumType.STRING)
    private PostingState postingState;

    @Column
    private String postingLink;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime scheduledAt;

    @Column
    private String mediaId;

    @OneToMany(mappedBy = "posting", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    public void updateScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public void updateMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public void updatePostingState(PostingState state) {
        this.postingState = state;
    }

    public void updatePostingContent(String content) { this.postingContent = content; }
    @Builder
    public Posting(Project project, SNSType postingType, String postingContent, String title,
                   PostingState postingState, String postingLink, String mediaId, LocalDateTime scheduledAt) {
        this.project       = project;
        this.postingType   = postingType;
        this.postingContent = postingContent;
        this.title = title;
        this.scheduledAt   = scheduledAt;
        this.postingState  = postingState;
        this.postingLink   = postingLink;
        this.mediaId       = mediaId;
    }

}
