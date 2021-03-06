package com.fhdufhdu.mim.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
// @NamedEntityGraphs({
// @NamedEntityGraph(name = "CommentReport.comment", attributeNodes = {
// @NamedAttributeNode("comment")
// })
// })
public class CommentReport {
    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @NotNull
    private String reportReason;

    @NotNull
    private Timestamp reportTimestamp;

    @NotNull
    @Column(columnDefinition = "boolean default false")
    private Boolean isConfirmed;
}
