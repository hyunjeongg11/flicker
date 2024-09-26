package com.flicker.user.user.application;

import com.flicker.user.common.exception.RestApiException;
import com.flicker.user.common.status.StatusCode;
import com.flicker.user.user.domain.UserConverter;
import com.flicker.user.user.domain.entity.BookmarkMovie;
import com.flicker.user.user.domain.entity.FavoriteMovie;
import com.flicker.user.user.domain.entity.UnlikeMovie;
import com.flicker.user.user.domain.entity.User;
import com.flicker.user.user.dto.MovieSeqListDto;
import com.flicker.user.user.dto.UserRegisterDto;
import com.flicker.user.user.infrastructure.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public MovieSeqListDto getBookmarkMovies(Integer userSeq) {
        User user = findUserSeqToUser(userSeq);
        List<Integer> movieSeqList = user.getBookmarkMovies().stream()
                .map(BookmarkMovie::getBookmarkMovieSeq)
                .toList();
        return new MovieSeqListDto(movieSeqList);
    }

    @Override
    public MovieSeqListDto getFavoriteMovies(Integer userSeq) {
        User user = findUserSeqToUser(userSeq);
        List<Integer> movieSeqList = user.getFavoriteMovies().stream()
                .map(FavoriteMovie::getMovieSeq)
                .toList();
        return new MovieSeqListDto(movieSeqList);
    }

    @Override
    @Transactional
    public MovieSeqListDto getUnlikeMovies(Integer userSeq) {
        User user = findUserSeqToUser(userSeq);
        List<Integer> movieSeqList = user.getUnlikeMovies().stream()
                .map(UnlikeMovie::getMovieSeq)
                .toList();
        return new MovieSeqListDto(movieSeqList);
    }

    @Override
    @Transactional
    public boolean deleteBookmarkMovie(Integer userSeq, Integer movieSeq) {
        User user = findUserSeqToUser(userSeq);
        return user.deleteBookmarkMovie(movieSeq);
    }

    @Override
    @Transactional
    public boolean deleteUnlikeMovie(Integer userSeq, Integer movieSeq) {
        User user = findUserSeqToUser(userSeq);

        return user.deleteUnlikeMovie(movieSeq);
    }

    @Override
    @Transactional
    public boolean registerBookmarkMovie(Integer userSeq, Integer movieSeq) {
        User user = findUserSeqToUser(userSeq);
        return user.addBookmarkMovie(movieSeq);
    }

    @Override
    @Transactional
    public boolean registerUnlikeMovie(Integer userSeq, Integer movieSeq) {
        User user = findUserSeqToUser(userSeq);
        return user.addUnlikeMovie(movieSeq);
    }

    @Override
    @Transactional
    public boolean registerFavoriteMovie(Integer userSeq, MovieSeqListDto dto) {
        User user = findUserSeqToUser(userSeq);
        user.addFavoriteMovie(dto);
        return true;
    }

    @Override
    @Transactional
    public boolean register(UserRegisterDto dto) {

        User findUser = userRepository.findByUserId(dto.getUserId());
        if(findUser != null) {
            throw new RestApiException(StatusCode.DUPLICATE_ID);
        }

        dto.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        User user = userConverter.registerDtoToUser(dto);
        System.out.println("저장 완료");
        userRepository.save(user);

        return true;
    }



    @Override
    @Transactional
    public boolean delete(Integer userSeq) {

        User user = userRepository.findById(userSeq)
                .orElseThrow(() -> new RestApiException(StatusCode.CAN_NOT_FIND_USER));
        user.deleteUser();

        return false;
    }

    public User findUserSeqToUser(Integer userSeq){
        User user = userRepository.findById(userSeq)
                .orElseThrow(() -> new RestApiException(StatusCode.CAN_NOT_FIND_USER));
        if(user.getIsActive() == 0){
            throw new RestApiException(StatusCode.INACTIVE_USER);
        }
        return user;
    }
}
