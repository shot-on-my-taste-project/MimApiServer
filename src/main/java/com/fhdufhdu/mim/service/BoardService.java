package com.fhdufhdu.mim.service;

import com.fhdufhdu.mim.dto.RequestBoardDto;
import com.fhdufhdu.mim.dto.board.BoardDto;

import org.springframework.data.domain.Page;

public interface BoardService {
    /**
     * GET /boards?page={page}
     * 영화 이름
     */
    Page<BoardDto> getAllBoards(int page, int size);

    /** GET /boards/title/{title}?page={page} */
    Page<BoardDto> getBoardsByTitle(String title, int page, int size);

    /** GET /boards/movie/{movieId} */
    BoardDto getBoardByMovieId(Long movieId);

    /** GET /boards/{id} */
    BoardDto getBoardById(Long id);

    /** DELETE /boards/{id} */
    void shutDownBoard(Long id);

    /** POST /request-boards/movie/{movieId} */
    void countUpBoard(Long movieId);

    /** POST /request-boards/{id} */
    void openUpBoard(Long requestId);

    /** DELETE /request-boards/{id} */
    void cancelRequestBoard(Long requestId);

    /** GET /request-boards?page={page} */
    Page<RequestBoardDto> getAllRequests(int page, int size);

    /** GET /request-boards/{id} */
    RequestBoardDto getRequestById(Long id);
}
