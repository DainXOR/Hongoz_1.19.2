package net.dain.hongozmod.colony;

import net.dain.hongozmod.colony.role.*;
import net.dain.hongozmod.util.RandomSelection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Colony<
        MemberType extends ColonyMember,
        QueenType extends LivingEntity & ColonyQueen,
        ColonyType extends Colony<MemberType, QueenType, ?>>{
    public static final int WORKER_BASE_CHANCE = 160;
    public static final int EXPLORER_BASE_CHANCE = 80;
    public static final int WARRIOR_BASE_CHANCE = 40;
    public static final int ROYAL_WARRIOR_BASE_CHANCE = 10;

    public static final int HEIR_BASE_CHANCE = 1;
    public static final int FOOD_STORE_BASE_CHANCE = 20;
    public static final float REJECT_BASE_CHANCE = 0.5f;

    protected RandomSelection<ColonyRoles> randomRole;

    public static final int DEFAULT_CAPACITY = 50;
    public static final int MIN_FAIL_GROW_ATTEMPTS = 10;
    public final int maxCapacity;
    protected int actualSize = 0;
    protected int growAttempts = 0;

    protected List<? extends MemberType> newborns = new ArrayList<>(10);
    protected List<? extends ColonyWorker> workers = new ArrayList<>(10);
    protected List<? extends ColonyExplorer> explorers = new ArrayList<>(10);
    protected List<? extends ColonyWarrior> warriors = new ArrayList<>(10);
    protected List<? extends ColonyRoyalWarrior> royalWarriors = new ArrayList<>(5);
    protected List<ColonyHeir> heirs = new ArrayList<>(3);
    protected QueenType ColonyQueen;
    protected int colonyAge = 0;

    protected final RandomSource random = RandomSource.create();
    protected final Class<ColonyType> colonyType;

    protected LivingEntity target = null;
    protected AlertLevel alertLevel = AlertLevel.NONE;

    public Colony(Class<ColonyType> colonyType, QueenType newQueen, int capacity){
        this.colonyType = colonyType;
        this.ColonyQueen = newQueen;
        this.maxCapacity = capacity;
    }
    public Colony(Class<ColonyType> colonyType, QueenType newQueen){
        this.colonyType = colonyType;
        this.ColonyQueen = newQueen;
        this.maxCapacity = DEFAULT_CAPACITY;
    }

    protected ColonyType growColony(){
        ColonyType newColony = this.colonyType.cast(new Colony<>(this.colonyType, this.getQueen(), this.maxCapacity + DEFAULT_CAPACITY));
        newColony.setMembers(this);
        newColony.setHeirs((List<MemberType>) this.getHeirs());
        newColony.colonyAge = this.colonyAge;
        newColony.actualSize = this.actualSize;
        newColony.setTarget(this.getTarget());

        return newColony;
    }

    public boolean requestColonyGrow(ColonyMember member){
        return this.getQueen() == member && this.getQueen().setColony(this.growColony());
    }

    public List<MemberType> getMembers(){
        List<ColonyMember> members = new ArrayList<>();
        members.addAll(workers);
        members.addAll(explorers);
        members.addAll(warriors);
        members.addAll(royalWarriors);

        return (List<MemberType>) members;
    }
    public List<MemberType> getAllMembers(){
        List<ColonyMember> members = new ArrayList<>();
        members.addAll(newborns);
        members.addAll(getMembers());
        members.add(this.getQueen());

        return (List<MemberType>) members;
    }

    public List<? extends MemberType> getNewborns(){
        return this.newborns;
    }
    public List<? extends ColonyWorker> getWorkers(){
        return this.workers;
    }
    public List<? extends ColonyExplorer> getExplorers(){
        return this.explorers;
    }
    public List<? extends ColonyWarrior> getWarriors(){
        return this.warriors;
    }
    public List<? extends ColonyRoyalWarrior> getRoyalWarriors(){
        return this.royalWarriors;
    }
    public List<? extends ColonyHeir> getHeirs(){
        return this.heirs;
    }

    public void setMembers(@Nullable List<? extends MemberType> newborns,
                           @Nullable List<? extends ColonyWorker> workers,
                           @Nullable List<? extends ColonyExplorer> explorers,
                           @Nullable List<? extends ColonyWarrior> warriors,
                           @Nullable List<? extends ColonyRoyalWarrior> royalWarriors){
        if(newborns != null){
            this.newborns = newborns;
        }
        if(workers != null){
            this.workers = workers;
        }if(explorers != null){
            this.explorers = explorers;
        }if(warriors != null){
            this.warriors = warriors;
        }if(royalWarriors != null){
            this.royalWarriors = royalWarriors;
        }

    }

    public void setMembers(Colony<MemberType, QueenType, ?> colony){
        this.setMembers(
                colony.getNewborns(),
                colony.getWorkers(),
                colony.getExplorers(),
                colony.getWarriors(),
                colony.getRoyalWarriors());
    }

    public void setHeirs(List<MemberType> heirs){
        List<ColonyHeir> newHeirs = new ArrayList<>();
        heirs.forEach(possibleHeir -> {
            if(possibleHeir instanceof ColonyWorker accepted){
                newHeirs.add(accepted);
                accepted.setRole(ColonyRoles.HEIR);
            }
        });
        this.heirs.clear();
        this.heirs = newHeirs;
    }
    public void addHeir(MemberType newHeir){
        if(newHeir instanceof ColonyWorker worker) {
            this.heirs.add(worker);
            this.actualSize += 1;
        }
    }
    public void addHeirs(List<MemberType> newHeirs){
        List<ColonyWorker> workers = new ArrayList<>();
        newHeirs.forEach(possibleHeir -> {
            if(possibleHeir instanceof ColonyWorker accepted){
                workers.add(accepted);
            }
        });
        this.heirs.addAll(workers);
        this.actualSize += newHeirs.size();
    }

    public @NotNull QueenType getQueen(){
        return this.ColonyQueen;
    }
    public boolean requestAdoption(@NotNull MemberType outsider){
        this.alertLevel = AlertLevel.MEDIUM;
        return this.addNewMember(outsider, true);
    }
    public boolean addChild(@NotNull MemberType newChild){
        return this.addNewMember(newChild, false);
    }
    public boolean addNewMember(@NotNull ColonyMember newMember, boolean outsider){
        if( (!outsider || this.approveMember(newMember)) &&
            this.assignRole(newMember) && newMember.setColony(this)){

            newMember.returnToQueen(AlertLevel.HIGH);
            return true;
        }

        return false;
    }
    @Contract("null->false")
    protected boolean approveMember(@Nullable ColonyMember newMember){
        MemberType m = (MemberType) newMember;
        return (m == null && this.random.nextFloat() > this.getRejectChance()) && newMember.getColony() == null;
    }
    protected boolean assignRole(@NotNull ColonyMember member){
        final int workerChance = WORKER_BASE_CHANCE + this.getQueen().getWorkerChance();
        final int explorerChance = EXPLORER_BASE_CHANCE + this.getQueen().getExplorerChance();
        final int warriorChance = WARRIOR_BASE_CHANCE + this.getQueen().getWarriorChance();
        final int royalWarriorChance = ROYAL_WARRIOR_BASE_CHANCE + this.getQueen().getRoyalWarriorChance();
        final int totalChance = workerChance + explorerChance + warriorChance + royalWarriorChance;

        ColonyRoles newRole = (new RandomSelection<ColonyRoles>())
                .add(ColonyRoles.WORKER, workerChance)
                .add(ColonyRoles.EXPLORER, explorerChance)
                .add(ColonyRoles.WARRIOR, warriorChance)
                .add(ColonyRoles.ROYAL_WARRIOR, royalWarriorChance)
                .next();

        if(newRole.equals(ColonyRoles.WORKER)){
            final int heirChance = HEIR_BASE_CHANCE + this.getQueen().getHeirChance();
            final int foodStoreChance = FOOD_STORE_BASE_CHANCE + this.getQueen().getFoodStoreChance();

            newRole = (new RandomSelection<ColonyRoles>())
                    .add(ColonyRoles.WORKER, workerChance)
                    .add(ColonyRoles.FOOD_SACK, foodStoreChance)
                    .add(ColonyRoles.HEIR, heirChance)
                    .next();
        }

        if(this.assignRole(member, newRole)){

        }
        return false;
    }
    protected boolean assignRole(@NotNull ColonyMember member, ColonyRoles role){
        MemberType m = member.changeRole(role);

        if(m == null){
            return false;
        }
        member = m;
        return true;
    }

    public float getRejectChance(){
        return (float)this.random.triangle(0.0f, REJECT_BASE_CHANCE) +
                this.getQueen().getRejectChance();
    }

    public int size(){
        return this.workers.size() + this.explorers.size() + this.warriors.size() + this.royalWarriors.size();
    }
    public int completeSize() {
        return this.size() + this.getNewborns().size() + 1;
    }

    public void queenDied(){
        this.alertLevel = AlertLevel.CRITICAL;
    }
    protected QueenType selectNewQueen(){
        this.heirs.sort(Comparator.comparingInt(ColonyHeir::queeningScore));
        ColonyHeir optimalHeir = this.heirs.get(0);

        QueenType newQueen = (QueenType) optimalHeir.becomeQueen();

        return newQueen;
    }

    public void setTarget(LivingEntity target){
        if(this.alertLevel.ordinal() < AlertLevel.MEDIUM.ordinal()){
            this.alertLevel = AlertLevel.MEDIUM;
        }
        this.alertLevel = AlertLevel.values()[this.alertLevel.ordinal() + 1];
        this.target = target;
    }
    public LivingEntity getTarget() {
        return this.target;
    }
    public boolean inAlert(){
        return this.target != null || this.alertLevel != AlertLevel.NONE;
    }
    public AlertLevel getAlertLevel(){
        return this.alertLevel;
    }
}
