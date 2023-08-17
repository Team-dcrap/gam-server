package com.gam.api.service.admin;


import com.gam.api.common.message.ExceptionMessage;
import com.gam.api.dto.admin.magazine.request.MagazineCreateRequestDTO;
import com.gam.api.dto.admin.magazine.response.MagazineListResponseDTO;

import com.gam.api.entity.Magazine;
import com.gam.api.entity.MagazinePhoto;
import com.gam.api.entity.Question;
import com.gam.api.repository.MagazinePhotoRepository;
import com.gam.api.repository.MagazineRepository;
import com.gam.api.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final MagazineRepository magazineRepository;
    private final MagazinePhotoRepository magazinePhotoRepository;
    private final QuestionRepository questionRepository;

    @Override
    public List<MagazineListResponseDTO> getMagazines() {
        return magazineRepository.findAllByOrderByModifiedAtDescCreatedAtDesc().stream()
                .map(MagazineListResponseDTO::of)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMagazine(Long magazineId) {
        magazineRepository.findById(magazineId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.NOT_FOUND_MAGAZINE.getMessage()));

        magazineRepository.deleteById(magazineId);
    }

    @Transactional
    @Override
    public void createMagazine(Long userId, MagazineCreateRequestDTO request) {
        val magazine = Magazine.builder()
                .thumbNail(request.magazinePhotos().get(0))
                .magazineTitle(request.title())
                .introduction(request.magazineIntro())
                .interviewPerson(request.interviewPerson())
                .build();
        magazineRepository.save(magazine);

        val magazinePhotos = request.magazinePhotos().stream()
                .map((photo)-> {
                    val magazinePhoto = MagazinePhoto.builder()
                            .url(photo)
                            .build();
                    magazinePhoto.setMagazine(magazine);
                    magazinePhotoRepository.save(magazinePhoto);
                    return magazinePhoto;
                }).collect(Collectors.toList());

        val questions = request.questions().stream()
                .map((questionVO) -> {
                            val question = Question.builder()
                                    .questionOrder(questionVO.questionOrder())
                                    .question(questionVO.question())
                                    .answer(questionVO.answer())
                                    .answerImage(questionVO.answerImage())
                                    .imageCaption(questionVO.imageCaption())
                                    .build();
                            question.setMagazine(magazine);
                            questionRepository.save(question);
                            return question;
                }).collect(Collectors.toList());

        magazine.setMagazinePhotos(magazinePhotos);
        magazine.setQuestions(questions);
    }
}