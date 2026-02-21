package api;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

public class Mesto1Test {

    // ВАЖНО: замените на реальный токен!
    String bearerToken = "ВАШ_РЕАЛЬНЫЙ_ТОКЕН";

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-mesto.praktikum-services.ru";
        ensureThereIsAtLeastOnePhoto();
    }

    private void ensureThereIsAtLeastOnePhoto() {
        var response = given()
                .auth().oauth2(bearerToken)
                .get("/api/cards");

        List<String> photos = response.jsonPath().getList("data");
        if (photos == null || photos.isEmpty()) {
            // Создаем тестовое фото
            given()
                    .header("Content-type", "application/json")
                    .auth().oauth2(bearerToken)
                    .body("{\"name\":\"Тестовое фото\",\"link\":\"https://code.s3.yandex.net/qa-automation-engineer/java/files/paid-track/sprint1/photoSelenium.jpg\"}")
                    .post("/api/cards");
        }
    }

    @Test
    @DisplayName("Add a new photo")
    @Description("This test is for adding a new photo to Mesto.")
    void addNewPhoto() {
        given()
                .header("Content-type", "application/json")
                .auth().oauth2(bearerToken)
                .body("{\"name\":\"Москва\",\"link\":\"https://code.s3.yandex.net/qa-automation-engineer/java/files/paid-track/sprint1/photoSelenium.jpg\"}")
                .post("/api/cards")
                .then().statusCode(201);
    }

    @Test
    @DisplayName("Like the first photo")
    @Description("This test is for liking the first photo on Mesto.")
    public void likeTheFirstPhoto() {
        String photoId = getTheFirstPhotoId();
        System.out.println("Found photo ID: " + photoId); // отладка

        likePhotoById(photoId);
        deleteLikePhotoById(photoId);
    }

    @Step("Take the first photo from the list")
    private String getTheFirstPhotoId() {
        String photoId = given()
                .auth().oauth2(bearerToken)
                .get("/api/cards")
                .then().extract().body().path("data[0]._id");

        if (photoId == null) {
            throw new RuntimeException("Не удалось получить ID фотографии. Возможно, у пользователя нет фото.");
        }

        return photoId;
    }

    @Step("Like a photo by id")
    private void likePhotoById(String photoId) {
        given()
                .auth().oauth2(bearerToken)
                .put("/api/cards/{photoId}/likes", photoId)
                .then().assertThat().statusCode(200);
    }

    @Step("Delete like from the photo by id")
    private void deleteLikePhotoById(String photoId) {
        given()
                .auth().oauth2(bearerToken)
                .delete("/api/cards/{photoId}/likes", photoId)
                .then().assertThat().statusCode(200);
    }
}