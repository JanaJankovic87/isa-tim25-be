package net.javaguides.springboot_jutjubic.service;

import net.javaguides.springboot_jutjubic.dto.VideoPopularityDTO;
import net.javaguides.springboot_jutjubic.model.PopularityResult;

import java.util.List;

public interface PopularityETLService {

    // izvlačenje, transformacija i čuvanje podataka o popularnosti videa
    void runDailyETLPipeline();

    // ova metoda može biti pozvana iz REST kontrolera ili testova
    // za Manualno pokretanje pipeline-a van redovnog rasporeda
    void runManually();

    // vraca top 3 najpopularnija videa iz poslednjeg izvršavanja pipeline-a
    List<PopularityResult> getTopVideos();

    // vraca top 3 najpopularnija videa sa kompletnim detaljima za prikaz na frontendu
    List<VideoPopularityDTO> getTopVideosForDisplay();
}