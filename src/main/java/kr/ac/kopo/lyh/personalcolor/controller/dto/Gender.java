package kr.ac.kopo.lyh.personalcolor.controller.dto;

public enum Gender {
    MAN("남성"), WOMAN("여성");

    private final String description;

    Gender(String description) {
        this.description = description;
    }

    public enum Role {
        USER("사용자"),
        ADMIN("관리자");

        private final String displayName;

        Role(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
    public String getDescription() {
        return description;
    }
}
