package com.example.demo.game;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BossSkillExecutor {

    private final Random random = new Random();

    /** roomId -> (skillCode -> 쿨타임 상태) */
    private final Map<String, Map<String, SkillState>> roomSkillStates = new ConcurrentHashMap<>();

    /**
     * maxCd    : 스킬 고유 쿨타임 (턴수). 1이면 사실상 "쿨타임 없음" 취급.
     * remaining: 남은 쿨타임 턴 수. 0이면 즉시 사용 가능.
     * available: == (remaining == 0)
     */
    private static class SkillState {
        int maxCd;
        int remaining;
        boolean available;
    }

    /** 클라이언트 UI용 쿨타임 스냅샷 */
    public static class SkillCooldownInfo {
        private int skill1CdNow;
        private int skill1CdMax;
        private boolean skill1Available;
        private String skill1Name;
        private String skill1Desc;

        private int skill2CdNow;
        private int skill2CdMax;
        private boolean skill2Available;
        private String skill2Name;
        private String skill2Desc;

        private int skill3CdNow;
        private int skill3CdMax;
        private boolean skill3Available;
        private String skill3Name;
        private String skill3Desc;

        public int getSkill1CdNow() { return skill1CdNow; }
        public void setSkill1CdNow(int skill1CdNow) { this.skill1CdNow = skill1CdNow; }
        public int getSkill1CdMax() { return skill1CdMax; }
        public void setSkill1CdMax(int skill1CdMax) { this.skill1CdMax = skill1CdMax; }
        public boolean isSkill1Available() { return skill1Available; }
        public void setSkill1Available(boolean skill1Available) { this.skill1Available = skill1Available; }
        public String getSkill1Name() { return skill1Name; }
        public void setSkill1Name(String skill1Name) { this.skill1Name = skill1Name; }
        public String getSkill1Desc() { return skill1Desc; }
        public void setSkill1Desc(String skill1Desc) { this.skill1Desc = skill1Desc; }

        public int getSkill2CdNow() { return skill2CdNow; }
        public void setSkill2CdNow(int skill2CdNow) { this.skill2CdNow = skill2CdNow; }
        public int getSkill2CdMax() { return skill2CdMax; }
        public void setSkill2CdMax(int skill2CdMax) { this.skill2CdMax = skill2CdMax; }
        public boolean isSkill2Available() { return skill2Available; }
        public void setSkill2Available(boolean skill2Available) { this.skill2Available = skill2Available; }
        public String getSkill2Name() { return skill2Name; }
        public void setSkill2Name(String skill2Name) { this.skill2Name = skill2Name; }
        public String getSkill2Desc() { return skill2Desc; }
        public void setSkill2Desc(String skill2Desc) { this.skill2Desc = skill2Desc; }

        public int getSkill3CdNow() { return skill3CdNow; }
        public void setSkill3CdNow(int skill3CdNow) { this.skill3CdNow = skill3CdNow; }
        public int getSkill3CdMax() { return skill3CdMax; }
        public void setSkill3CdMax(int skill3CdMax) { this.skill3CdMax = skill3CdMax; }
        public boolean isSkill3Available() { return skill3Available; }
        public void setSkill3Available(boolean skill3Available) { this.skill3Available = skill3Available; }
        public String getSkill3Name() { return skill3Name; }
        public void setSkill3Name(String skill3Name) { this.skill3Name = skill3Name; }
        public String getSkill3Desc() { return skill3Desc; }
        public void setSkill3Desc(String skill3Desc) { this.skill3Desc = skill3Desc; }
    }

    // ===== 내부 유틸 =====

    private Map<String, SkillState> getSkillStateMap(String roomId) {
        return roomSkillStates.computeIfAbsent(roomId, id -> new ConcurrentHashMap<>());
    }

    private String codeOf(BossSkill skill) {
        if (skill == null || skill.getName() == null) return "";
        return skill.getName().trim().toLowerCase();
    }

    /** 스킬 코드에 따라 "몇 턴마다" 사용 가능한지 정의 */
    private int resolveMaxCd(BossSkill skill) {
        String code = codeOf(skill);
        return switch (code) {
            case "highattack", "강공" -> 2; // 2턴마다 (사용 -> 2 -> 1 -> 0=ready)
            case "breath", "브레스"   -> 1; // 쿨타임 없음 (매턴 사용 가능)
            default                  -> 1; // 기본: 쿨타임 없음
        };
    }

    private SkillState getOrCreateState(String roomId, BossSkill skill) {
        String code = codeOf(skill);
        Map<String, SkillState> map = getSkillStateMap(roomId);
        return map.computeIfAbsent(code, c -> {
            SkillState st = new SkillState();
            st.maxCd = resolveMaxCd(skill);
            st.remaining = 0;     // 처음엔 바로 사용 가능
            st.available = true;  // == remaining == 0
            return st;
        });
    }

    // ===== 공개 API들 =====

    /**
     * 이번 턴에 사용할 스킬 선택 (+ 내부 쿨타임 갱신)
     * - 우선순위 : skill3 > skill2 > skill1
     */
    public BossSkill chooseSkillForTurn(String roomId, BossMonster boss) {
        if (boss == null) return null;

        // 0) 우선순위 높은 순으로 리스트 구성
        List<BossSkill> ordered = new ArrayList<>();
        if (boss.getSkill3() != null) ordered.add(boss.getSkill3()); // 1순위
        if (boss.getSkill2() != null) ordered.add(boss.getSkill2()); // 2순위
        if (boss.getSkill1() != null) ordered.add(boss.getSkill1()); // 3순위

        if (ordered.isEmpty()) return null;

        // 1) 턴 시작 시 쿨타임 감소
        for (BossSkill s : ordered) {
            SkillState st = getOrCreateState(roomId, s);

            // maxCd <= 1 이면 쿨타임 없음 → 항상 ready 상태 유지
            if (st.maxCd <= 1) {
                st.remaining = 0;
                st.available = true;
                continue;
            }

            if (!st.available && st.remaining > 0) {
                st.remaining--;
                if (st.remaining <= 0) {
                    st.remaining = 0;
                    st.available = true;
                }
            }
        }

        // 2) 우선순위대로 available == true 인 첫 번째 스킬 선택
        for (BossSkill s : ordered) {
            SkillState st = getOrCreateState(roomId, s);
            if (st.available) {
                // 쿨타임 있는 스킬만 다시 잠그기
                if (st.maxCd > 1) {
                    st.remaining = st.maxCd;
                    st.available = false;
                } else {
                    // 쿨타임 없는 스킬은 계속 ready 상태
                    st.remaining = 0;
                    st.available = true;
                }
                return s;
            }
        }

        // 3) 사용할 수 있는 스킬이 하나도 없으면 null (기본 평타 로직으로 fallback)
        return null;
    }

    /**
     * 실제 공격 계획 생성
     * - turnNumber는 쿨타임에는 관여하지 않고, 연출용으로만 사용 가능
     */
    public Map<Long, Integer> createAttackPlan(BossSkill skill,
                                               List<PartyMemberView> members,
                                               int turnNumber) {
        Map<Long, Integer> plan = new HashMap<>();
        if (skill == null || members == null || members.isEmpty()) {
            return plan;
        }

        String code = codeOf(skill);

        switch (code) {
            // 브레스: 한국어/영어 둘 다 허용
            case "breath", "브레스" -> applyBreath(plan, members);

            // 강공: 한국어/영어 둘 다 허용
            case "highattack", "강공" -> applyHighAttack(plan, members);

            default -> {
                // fallback: 전원 10 데미지
                for (PartyMemberView view : members) {
                    plan.put(view.getCharacterId(), 10);
                }
            }
        }

        return plan;
    }

    /**
     * 🔥 UI용 쿨타임 스냅샷 (읽기 전용)
     * - roomId 와 BossMonster(= skill1/2/3) 기준으로,
     *   현재 SkillState를 그대로 매핑해서 돌려준다.
     */
    public SkillCooldownInfo getSkillCooldownInfo(String roomId, BossMonster boss) {
        SkillCooldownInfo info = new SkillCooldownInfo();
        if (boss == null) return info;

        applySnapshotPerSlot(info, 1, boss.getSkill1(), roomId);
        applySnapshotPerSlot(info, 2, boss.getSkill2(), roomId);
        applySnapshotPerSlot(info, 3, boss.getSkill3(), roomId);

        return info;
    }

    private void applySnapshotPerSlot(SkillCooldownInfo info,
                                      int slotIndex,
                                      BossSkill skill,
                                      String roomId) {
        if (skill == null) return;

        SkillState st = getOrCreateState(roomId, skill);

        int cdNow = st.remaining;   // 남은 턴 수 그대로
        int cdMax = st.maxCd;       // 쿨타임 설정값
        boolean available = st.available;
        String name = skill.getName();
        String desc = skill.getDescription();

        switch (slotIndex) {
            case 1 -> {
                info.setSkill1CdNow(cdNow);
                info.setSkill1CdMax(cdMax);
                info.setSkill1Available(available);
                info.setSkill1Name(name);
                info.setSkill1Desc(desc);
            }
            case 2 -> {
                info.setSkill2CdNow(cdNow);
                info.setSkill2CdMax(cdMax);
                info.setSkill2Available(available);
                info.setSkill2Name(name);
                info.setSkill2Desc(desc);
            }
            case 3 -> {
                info.setSkill3CdNow(cdNow);
                info.setSkill3CdMax(cdMax);
                info.setSkill3Available(available);
                info.setSkill3Name(name);
                info.setSkill3Desc(desc);
            }
        }
    }

    // ===== 개별 스킬 로직 =====

    /**
     * breath : 2인 3d5 공격
     */
    private void applyBreath(Map<Long, Integer> plan,
                             List<PartyMemberView> members) {

        if (members.isEmpty()) return;

        List<PartyMemberView> shuffled = new ArrayList<>(members);
        Collections.shuffle(shuffled, random);

        int targetCount = Math.min(2, shuffled.size());
        for (int i = 0; i < targetCount; i++) {
            PartyMemberView view = shuffled.get(i);
            int dmg = roll3d5();
            plan.put(view.getCharacterId(), dmg);
        }
    }

    /**
     * highattack : 사용 시 1인에게 40 고정 데미지
     * (턴 간격은 쿨타임으로 이미 보장됨)
     */
    private void applyHighAttack(Map<Long, Integer> plan,
                                 List<PartyMemberView> members) {

        if (members.isEmpty()) return;

        PartyMemberView target = members.get(random.nextInt(members.size()));
        plan.put(target.getCharacterId(), 40);
    }

    /** 3d5 : 1~5를 3번 굴려 합산 */
    private int roll3d5() {
        int sum = 0;
        for (int i = 0; i < 3; i++) {
            sum += random.nextInt(5) + 1; // 1~5
        }
        return sum;
    }
}
