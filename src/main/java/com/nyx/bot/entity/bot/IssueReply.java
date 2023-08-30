package com.nyx.bot.entity.bot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问答表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table
public class IssueReply extends BaseEntity {

    @GeneratedValue
    @Id
    Long id;

    @JsonProperty("issue")
    String issue;

    @JsonProperty("issue_face")
    String issueFace;

    @JsonProperty("issue_image")
    String issueImage;

    @JsonProperty("reply")
    String reply;

    @JsonProperty("reply_face")
    String replyFace;

    @JsonProperty("reply_image")
    String replyImage;

}
