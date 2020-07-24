package src.main.java.com.googleintern.wfm.ruleengine.action;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import src.main.java.com.googleintern.wfm.ruleengine.model.UserModel;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

/** DataProcessor class is used to filter out invalid or conflict data. */
public class DataProcessor {

  /** Filter out user data with invalid workgroup Id values(< 0). */
  public static ImmutableList<UserModel> filterUsersWithValidWorkgroupId(
      ImmutableList<UserModel> rawData) {
    ImmutableList.Builder<UserModel> validDataBuilder = ImmutableList.<UserModel>builder();

    for (final UserModel data : rawData) {
      if (data.workgroupId() > 0) validDataBuilder.add(data);
    }
    return validDataBuilder.build();
  }

  /**
   * Filter out conflict user data. Conflict users are users with less role/skill ids but more
   * assigned permissions. This function is used when we want final generated rules to assign less
   * permissions in conflict cases.
   *
   * <p>Example for conflict users: User0, 1, 2 are from the same workforce and the same workgroup.
   * User 0 has {role id = 1111, skill ids = 2222, 3333} and is assigned permissions = {AAAA, BBBB}.
   * User 1 has {role id = 1111, skill ids = 2222} and is assigned permissions = {AAAA, BBBB, CCCC}.
   * User 2 has {role id = 1111, skill ids = 2222} and is assigned permissions = {AAAA, CCCC}. Both
   * User 1 and 2 are conflict users with respect to User 0. Types of Skill/role ids for User 1 and
   * 2 are included in the types for User 0. However, User 1 and 2 have a different permission CCCC
   * that is not included in User 0.
   */
  public static ImmutableList<UserModel> removeConflictUsers(ImmutableList<UserModel> rawUserData) {
    ImmutableSet<Long> coveredConflictUsers =
        collectAllCoveredConflictUserIds(findConflictUserIdPairs(rawUserData));
    return rawUserData.stream()
        .filter(user -> !coveredConflictUsers.contains(user.userId()))
        .collect(toImmutableList());
  }

  private static ImmutableSet<Long> collectAllCoveredConflictUserIds(
      ImmutableSetMultimap<Long, Long> conflictUserPairs) {
    ImmutableSet.Builder<Long> coveredConflictUsers = ImmutableSet.builder();
    conflictUserPairs
        .keySet()
        .forEach(key -> coveredConflictUsers.addAll(conflictUserPairs.get(key)));
    return coveredConflictUsers.build();
  }

  private static ImmutableSetMultimap<Long, Long> findConflictUserIdPairs(
      ImmutableList<UserModel> rawUserData) {
    ImmutableSetMultimap.Builder<Long, Long> conflictUserPairsBuilder =
        ImmutableSetMultimap.builder();
    rawUserData.forEach(
        currentUser ->
            conflictUserPairsBuilder.putAll(
                currentUser.userId(),
                rawUserData.stream()
                    .filter(comparedUser -> currentUser.isConflictUserPair(comparedUser))
                    .map(comparedUser -> comparedUser.userId())
                    .collect(toImmutableSet())));
    return conflictUserPairsBuilder.build();
  }
}
