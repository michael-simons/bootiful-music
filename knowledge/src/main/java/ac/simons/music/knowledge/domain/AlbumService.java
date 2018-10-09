package ac.simons.music.knowledge.domain;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * @author Michael J. Simons
 */
@Service
@RequiredArgsConstructor
public class AlbumService {
	private final AlbumRepository albumRepository;

	public List<AlbumEntity> findAllByArtistNameLike(String artistName) {
		return this.albumRepository.findAllByArtistNameLike(artistName, Sort.by("name").ascending(), 1);
	}
}
