package project.kristiyan.utilities;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import project.kristiyan.models.EmbedModel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmbedUtility {
    private static final int fieldsPerPage = 3;

    private Color getColor(String colorS) {
        Color color;
        switch (colorS.toLowerCase()) {
            case "black":
                color = Color.BLACK;
                break;
            case "blue":
                color = Color.BLUE;
                break;
            case "cyan":
                color = Color.CYAN;
                break;
            case "darkgray":
                color = Color.DARK_GRAY;
                break;
            case "gray":
                color = Color.GRAY;
                break;
            case "green":
                color = Color.GREEN;
                break;
            case "yellow":
                color = Color.YELLOW;
                break;
            case "lightgray":
                color = Color.LIGHT_GRAY;
                break;
            case "magneta":
                color = Color.MAGENTA;
                break;
            case "orange":
                color = Color.ORANGE;
                break;
            case "pink":
                color = Color.PINK;
                break;
            case "red":
                color = Color.RED;
                break;
            case "white":
                color = Color.WHITE;
                break;
            default:
                color = Color.green;
        }
        return color;
    }

    public List<MessageEmbed> buildEmbedsWithPagination(String content) {
        EmbedModel model;
        try {
            model = Utility.objectMapper.readValue(content, EmbedModel.class);

        } catch (Exception ignored) {
            return new ArrayList<>();
        }

        List<MessageEmbed> embeds = new ArrayList<>();
        List<MessageEmbed.Field> processedFields = new ArrayList<>();

        for (Map<String, Object> field : model.fields) {
            boolean inline = Boolean.parseBoolean(String.valueOf(field.getOrDefault("inline", false)));

            // Split name and value if needed
            java.util.List<String> nameChunks = _splitIntoChunks(String.valueOf(field.getOrDefault("name", "")));
            java.util.List<String> valueChunks = _splitIntoChunks(String.valueOf(field.getOrDefault("value", "")));

            // Pair them as needed
            if (nameChunks.size() == 1 && valueChunks.size() == 1) {
                processedFields.add(new MessageEmbed.Field(
                        nameChunks.getFirst(),
                        valueChunks.getFirst(),
                        inline
                ));

            } else if (nameChunks.size() == 1) {
                // values are more, then names
                processedFields.add(new MessageEmbed.Field(
                        nameChunks.getFirst(),
                        valueChunks.getFirst(),
                        inline
                ));

                valueChunks.removeFirst();

                for (String valuePart : valueChunks) {
                    processedFields.add(new MessageEmbed.Field(
                            ".",
                            valuePart,
                            inline
                    ));
                }
            } else if (valueChunks.size() == 1) {
                // for sure names are more, then values
                String last = nameChunks.getLast();
                nameChunks.removeLast();

                for (String namePart : nameChunks) {
                    processedFields.add(new MessageEmbed.Field(
                            namePart,
                            ".",
                            inline
                    ));
                }

                processedFields.add(new MessageEmbed.Field(
                        last,
                        valueChunks.getFirst(),
                        inline
                ));


            } else {
                // both are super long
                String last = nameChunks.getLast();
                nameChunks.removeLast();

                for (String namePart : nameChunks) {
                    processedFields.add(new MessageEmbed.Field(
                            namePart,
                            ".",
                            inline
                    ));
                }

                processedFields.add(new MessageEmbed.Field(
                        last,
                        valueChunks.getFirst(),
                        inline
                ));

                valueChunks.removeFirst();

                for (String valuePart : valueChunks) {
                    processedFields.add(new MessageEmbed.Field(
                            ".",
                            valuePart,
                            inline
                    ));
                }
            }
        }

        // Create embeds with 5 fields per page
        int totalFields = processedFields.size();
        int totalPages = (int) Math.ceil(totalFields / (double) fieldsPerPage);

        for (int page = 0; page < totalPages; page++) {
            EmbedBuilder embedBuilder = getEmbedBuilder(model);
            embedBuilder.setFooter(model.footer + " Page " + (page + 1) + " of " + totalPages);

            int startIdx = page * fieldsPerPage;
            int endIdx = Math.min(startIdx + fieldsPerPage, totalFields);

            for (int i = startIdx; i < endIdx; i++) {
                embedBuilder.addField(processedFields.get(i));
            }

            embeds.add(embedBuilder.build());
        }

        return embeds;
    }

    private java.util.List<String> _splitIntoChunks(String value) {
        List<String> split = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        int word_count = 0;
        for (String word : value.split(" ")) {
            word_count++;
            stringBuilder.append(word).append(" ");

            if (word_count % 100 == 0) {
                split.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            }
        }

        if (split.isEmpty()) {
            split.add(stringBuilder.toString());
        }

        return split;
    }

    private EmbedBuilder getEmbedBuilder(EmbedModel model) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (!model.color.isEmpty()) {
            embedBuilder.setColor(getColor(model.color));
        }

        if (!model.author.isEmpty()) {
            embedBuilder.setAuthor(model.author);
        }

        if (!model.footer.isEmpty()) {
            embedBuilder.setFooter(model.footer);
        }

        if (!model.url.isEmpty()) {
            embedBuilder.setUrl(model.url);
        }

        if (!model.image.isEmpty()) {
            embedBuilder.setImage(model.image);
        }
        return embedBuilder;
    }
}
