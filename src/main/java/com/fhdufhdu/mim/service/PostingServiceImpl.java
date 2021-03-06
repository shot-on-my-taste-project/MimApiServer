package com.fhdufhdu.mim.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fhdufhdu.mim.dto.comment.CommentAddDto;
import com.fhdufhdu.mim.dto.comment.CommentDto;
import com.fhdufhdu.mim.dto.comment.CommentModifyDto;
import com.fhdufhdu.mim.dto.posting.PostingAddDto;
import com.fhdufhdu.mim.dto.posting.PostingDto;
import com.fhdufhdu.mim.dto.posting.PostingModifyDto;
import com.fhdufhdu.mim.dto.posting.PostingUserDto;
import com.fhdufhdu.mim.entity.Board;
import com.fhdufhdu.mim.entity.Comment;
import com.fhdufhdu.mim.entity.Movie;
import com.fhdufhdu.mim.entity.Posting;
import com.fhdufhdu.mim.entity.Role;
import com.fhdufhdu.mim.entity.User;
import com.fhdufhdu.mim.exception.MismatchAuthorException;
import com.fhdufhdu.mim.exception.NotFoundBoardException;
import com.fhdufhdu.mim.exception.NotFoundCommentException;
import com.fhdufhdu.mim.exception.NotFoundPostingException;
import com.fhdufhdu.mim.exception.NotFoundUserException;
import com.fhdufhdu.mim.repository.BoardRepository;
import com.fhdufhdu.mim.repository.CommentRepository;
import com.fhdufhdu.mim.repository.PostingRepository;
import com.fhdufhdu.mim.repository.UserRepository;
import com.fhdufhdu.mim.service.util.UtilService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
// 페이징 구현해야함
public class PostingServiceImpl extends UtilService implements PostingService {
    private final PostingRepository postingRepository;
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Override
    public Page<PostingDto> getAllPostings(Long boardId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "postingNumber");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Posting> sources = postingRepository.findByBoardId(boardId, pageRequest);
        return convertToDests(sources, PostingDto.class);
    }

    @Override
    public PostingDto getPostingByBoardAndPostingNum(Long boardId, Long postingNumber) {
        Posting source = postingRepository.findByBoardIdAndPostingNumber(boardId, postingNumber)
                .orElseThrow(NotFoundPostingException::new);
        return convertToDest(source, PostingDto.class);
    }

    @Override
    public Page<PostingDto> getPostingByQuery(Long boardId, String query, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "postingNumber");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Posting> sources = postingRepository.findByBoardIdAndQuery(boardId, query, pageRequest);
        return convertToDests(sources, PostingDto.class);
    }

    @Override
    public PostingDto getPostingById(Long id) {
        Posting source = postingRepository.findById(id).orElseThrow(NotFoundPostingException::new);
        return convertToDest(source, PostingDto.class);
    }

    @Override
    public Page<PostingUserDto> getPostingsByUserId(String userId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "time");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Posting> sources = postingRepository.findByUserId(userId, pageRequest);

        List<Long> boardIds = new ArrayList<>();

        Map<Long, Board> boardMap = new HashMap<>();
        Map<Long, Movie> MovieMap = new HashMap<>();

        sources.forEach(x -> {
            boardIds.add(x.getMovieBoard().getId());
            boardMap.put(x.getId(), x.getMovieBoard());
        });
        List<Board> boards = boardRepository.findByIds(boardIds);
        boards.forEach(x -> {
            MovieMap.put(x.getId(), x.getMovie());
        });

        Page<PostingUserDto> dtos = sources.map(x -> {
            PostingUserDto dto = convertToDest(x, PostingUserDto.class);

            Long postingId = x.getId();
            Board board = boardMap.get(postingId);
            Movie movie = MovieMap.get(board.getId());

            dto.setMovieId(movie.getId());
            return dto;
        });

        return dtos;
    }

    @Override
    public void modifyPosting(Long id, PostingModifyDto postingDto) {
        Posting originalPosting = postingRepository.findById(id).orElseThrow(NotFoundPostingException::new);

        if (!checkUserId(originalPosting, getUserId()) && !hasAuthority(Role.ADMIN)) {
            throw new MismatchAuthorException();
        }

        originalPosting.setTitle(postingDto.getTitle());
        originalPosting.setContent(postingDto.getContent());
        originalPosting.setTime(getNowTimestamp());
        postingRepository.save(originalPosting);
    }

    @Override
    public void removePosting(Long id) {
        Posting originalPosting = postingRepository.findById(id).orElseThrow(NotFoundPostingException::new);

        if (!checkUserId(originalPosting, getUserId()) && !hasAuthority(Role.ADMIN)) {
            throw new MismatchAuthorException();
        }

        originalPosting.setIsRemoved(true);
        postingRepository.save(originalPosting);
    }

    @Override
    public void addPosting(PostingAddDto postingDto) {
        Board board = boardRepository.findById(postingDto.getMovieBoardId())
                .orElseThrow(NotFoundBoardException::new);
        board.setLastPostingNumber(board.getLastPostingNumber() + 1);
        User user = userRepository.findById(postingDto.getUserId()).orElseThrow(NotFoundUserException::new);

        if (!user.getId().equals(getUserId())) {
            throw new MismatchAuthorException();
        }

        Posting newPosting = Posting.builder()
                .movieBoard(board)
                .postingNumber(board.getLastPostingNumber())
                .title(postingDto.getTitle())
                .content(postingDto.getContent())
                .time(getNowTimestamp())
                .user(user)
                .commentCnt(0L)
                .isRemoved(false)
                .build();
        postingRepository.save(newPosting);
    }

    @Override
    public Page<CommentDto> getAllCommentsByBoardAndPostingNum(Long boardId, Long postingNumber, int page, int size) {
        List<Order> sortList = new ArrayList<>();
        sortList.add(Order.asc("commentGroup"));
        sortList.add(Order.asc("depth"));

        Sort sort = Sort.by(sortList);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Comment> comments = commentRepository.findByBoardIdAndPostingNum(boardId, postingNumber, pageRequest);
        return convertToDests(comments, CommentDto.class);
    }

    @Override
    public Page<CommentDto> getAllCommentsByPostingId(Long postingId, int page, int size) {
        List<Order> sortList = new ArrayList<>();
        sortList.add(Order.asc("commentGroup"));
        sortList.add(Order.asc("depth"));

        Sort sort = Sort.by(sortList);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Comment> comments = commentRepository.findByPostingId(postingId, pageRequest);
        return convertToDests(comments, CommentDto.class);
    }

    @Override
    public Page<CommentDto> getCommentsByUserId(String userId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "time");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Comment> sources = commentRepository.findByUserId(userId, pageRequest);
        return convertToDests(sources, CommentDto.class);
    }

    @Override
    public void modifyComment(Long commentId, CommentModifyDto commentDto) {
        Comment originalComment = commentRepository.findById(commentId).orElseThrow(NotFoundCommentException::new);

        if (!checkUserId(originalComment, getUserId()) && !hasAuthority(Role.ADMIN)) {
            throw new MismatchAuthorException();
        }

        originalComment.setContent(commentDto.getContent());
        originalComment.setTime(getNowTimestamp());
        commentRepository.save(originalComment);
    }

    @Override
    public void removeComment(Long commentId) {
        // 아래 두줄 n+1 문제 발생함
        Comment originalComment = commentRepository.findById(commentId).orElseThrow(NotFoundCommentException::new);
        Posting parentComment = originalComment.getPosting();

        parentComment.setCommentCnt(parentComment.getCommentCnt() - 1);

        if (!checkUserId(originalComment, getUserId()) && !hasAuthority(Role.ADMIN)) {
            throw new MismatchAuthorException();
        }

        originalComment.setIsRemoved(true);
        commentRepository.save(originalComment);
    }

    @Override
    public void addComment(CommentAddDto commentDto) {
        Posting posting = postingRepository.findById(commentDto.getPostingId())
                .orElseThrow(NotFoundPostingException::new);
        User user = userRepository.findById(commentDto.getUserId()).orElseThrow(NotFoundUserException::new);

        if (!user.getId().equals(getUserId())) {
            throw new MismatchAuthorException();
        }
        Comment newComment = Comment.builder()
                .content(commentDto.getContent())
                .isRemoved(false)
                .time(getNowTimestamp())
                .posting(posting)
                .commentGroup(100L)
                .depth(commentDto.getDepth())
                .user(user)
                .build();

        if (commentDto.getDepth() == 0) {
            Comment saved = commentRepository.save(newComment);
            saved.setCommentGroup(saved.getId());
            commentRepository.save(saved);
        } else {
            newComment.setCommentGroup(commentDto.getCommentGroup());
            commentRepository.save(newComment);
        }

        posting.setCommentCnt(posting.getCommentCnt() + 1);
    }

    private boolean checkUserId(Posting object, String userId) {
        return object.getUser().getId().equals(userId);
    }

    private boolean checkUserId(Comment object, String userId) {
        return object.getUser().getId().equals(userId);
    }
}
