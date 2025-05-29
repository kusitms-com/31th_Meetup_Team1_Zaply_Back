package org.zapply.product.domain.posting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.zapply.product.global.BaseTimeEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTimeEntity {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "posting_id", nullable = false)
    private Posting posting;

    @Column
    private String imageUrl;

    @Builder
    public Image(Posting posting, String imageUrl) {
        this.posting  = posting;
        this.imageUrl = imageUrl;
    }
}