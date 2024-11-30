package com.gam.api.common;

import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static java.util.Arrays.asList;

import com.slack.api.webhook.Payload;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SlackErrorPayload {

    private Payload payload;

    @Builder
    private SlackErrorPayload(Payload payload) {
        this.payload = payload;
    }

    public static Payload of(String errorDto) {
        // Create Payload with a code block
        String codeBlock = "```\n" +errorDto +  "```";

        return Payload.builder()
                .blocks(asList(
                        section(s -> s.text(markdownText(mt -> mt.text(codeBlock))))
                ))
                .build();
    }
}