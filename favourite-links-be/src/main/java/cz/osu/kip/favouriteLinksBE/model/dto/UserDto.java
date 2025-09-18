package cz.osu.kip.favouriteLinksBE.model.dto;

public record UserDto(int id, String keycloakId, String email, boolean isAdmin) {
}
