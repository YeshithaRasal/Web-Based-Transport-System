package lk.swiftgolanka.dto;

import jakarta.validation.constraints.NotBlank;

public class ComplaintDTO {

    private Long tripId;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Description is required")
    private String description;

    public ComplaintDTO() {}

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
