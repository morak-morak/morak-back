package com.morak.back.poll.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.morak.back.auth.domain.Member;
import com.morak.back.auth.domain.MemberRepository;
import com.morak.back.auth.domain.Team;
import com.morak.back.auth.domain.TeamRepository;
import com.morak.back.poll.domain.Poll;
import com.morak.back.poll.domain.PollItem;
import com.morak.back.poll.domain.PollItemRepository;
import com.morak.back.poll.domain.PollRepository;
import com.morak.back.poll.domain.PollStatus;
import com.morak.back.poll.ui.dto.PollCreateRequest;
import com.morak.back.poll.ui.dto.PollResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class PollService {

    private final PollRepository pollRepository;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final PollItemRepository pollItemRepository;

    private String tempCode = "ABCD";

    public Long createPoll(Long teamId, Long memberId, PollCreateRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Team team = teamRepository.findById(teamId).orElseThrow();

        Poll savedPoll = pollRepository.save(request.toPoll(member, team, PollStatus.OPEN, tempCode));
        List<PollItem> items = request.toPollItems(savedPoll);
        pollItemRepository.saveAll(items);

        return savedPoll.getId();
    }

    public List<PollResponse> findPolls(Long teamId, Long memberId) {
        Member member = memberRepository.getById(memberId);
        List<Poll> polls = pollRepository.findAllByTeamIdAndHostId(teamId, memberId);

        return polls.stream()
            .map(poll -> PollResponse.from(poll, member))
            .collect(Collectors.toList());
    }

    public void doPoll(Long tempMemberId, Long pollId, List<Long> itemIds) {
        Member member = memberRepository.getById(tempMemberId);
        Poll poll = pollRepository.getById(pollId);
        List<PollItem> items = pollItemRepository.findAllById(itemIds);
        poll.doPoll(items, member);
    }
}
