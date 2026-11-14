package ru.itmo.wp.model.service;

import com.google.common.base.Strings;
import ru.itmo.wp.model.domain.Talk;
import ru.itmo.wp.model.exception.ValidationException;
import ru.itmo.wp.model.repository.TalkRepository;
import ru.itmo.wp.model.repository.UserRepository;
import ru.itmo.wp.model.repository.impl.TalkRepositoryImpl;
import ru.itmo.wp.model.repository.impl.UserRepositoryImpl;

import java.util.List;

public class TalkService {
    private static final int MAX_TEXT_LENGTH = 4000;

    private final TalkRepository talkRepository = new TalkRepositoryImpl();
    private final UserRepository userRepository = new UserRepositoryImpl();

    public void validateSend(long sourceUserId, String targetUserIdRaw, String text) throws ValidationException {
        if (Strings.isNullOrEmpty(targetUserIdRaw)) {
            throw new ValidationException("Recipient is required");
        }
        long targetUserId;
        try {
            targetUserId = Long.parseLong(targetUserIdRaw);
        } catch (NumberFormatException e) {
            throw new ValidationException("Recipient is invalid");
        }
        if (targetUserId == sourceUserId) {
            throw new ValidationException("Can't send a message to yourself");
        }
        if (userRepository.find(targetUserId) == null) {
            throw new ValidationException("Recipient is not found");
        }
        if (Strings.isNullOrEmpty(text)) {
            throw new ValidationException("Text is required");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new ValidationException("Text can't be longer than " + MAX_TEXT_LENGTH + " characters");
        }
    }

    public void send(long sourceUserId, long targetUserId, String text) {
        Talk talk = new Talk();
        talk.setSourceUserId(sourceUserId);
        talk.setTargetUserId(targetUserId);
        talk.setText(text);
        talkRepository.save(talk);
    }

    public List<Talk> findAllByUserId(long userId) {
        return talkRepository.findAllByUserId(userId);
    }
}
