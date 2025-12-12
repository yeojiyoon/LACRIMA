package com.example.demo.game;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final PlayerCharacterRepository playerCharacterRepository;

    public MemberService(PlayerCharacterRepository playerCharacterRepository) {
        this.playerCharacterRepository = playerCharacterRepository;
    }

    @Transactional(readOnly = true)
    public List<MemberListView> getAllNonAdminMembers() {
        return playerCharacterRepository.findAllNonAdminWithSkills()
                .stream()
                .map(this::toView)
                .sorted(Comparator.comparing(MemberListView::getCharacterId))
                .collect(Collectors.toList());   // ✅ Java 8~ OK
    }

    private MemberListView toView(PlayerCharacter pc) {
        MemberListView v = new MemberListView();

        v.setCharacterId(pc.getId());
        v.setName(pc.getName());

        v.setAtk(pc.getAtk());
        v.setIntStat(pc.getIntelligence());
        v.setDet(pc.getDet());
        v.setHpStat(pc.getHp()); // HP 스탯(티어)

        v.setHp(pc.getCurrentHp());
        v.setMaxHp(pc.getMaxHp());

        int ratio = 0;
        if (pc.getMaxHp() > 0) {
            ratio = (int) Math.round((pc.getCurrentHp() * 100.0) / pc.getMaxHp());
            ratio = Math.max(0, Math.min(100, ratio));
        }
        v.setHpRatio(ratio);

        v.setAvatarUrl(pc.getAvatarUrl());

        // ✅ skillInventory(Set<Skill>) → skills(List<SkillView>)
        // null 방어 + tag/cooldown 포함
        List<MemberListView.SkillView> skillViews =
                (pc.getSkillInventory() == null ? List.<Skill>of() : pc.getSkillInventory())
                        .stream()
                        .map(s -> new MemberListView.SkillView(
                                s.getCode(),
                                s.getName(),
                                pickDesc(s),
                                s.getImageUrl(),
                                (s.getTag() != null ? s.getTag().name() : "UNKNOWN"),
                                s.getCooldown()
                        ))
                        .collect(Collectors.toList());

        v.setSkills(skillViews);

        return v;
    }

    private String pickDesc(Skill s) {
        if (s.getDescription() != null && !s.getDescription().isBlank()) return s.getDescription();
        if (s.getEffectText() != null && !s.getEffectText().isBlank()) return s.getEffectText();
        return "";
    }
}
