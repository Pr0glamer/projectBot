package nix.projectbot.repository;

import nix.projectbot.model.User;
import nix.projectbot.model.UserData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDataRepository extends CrudRepository<UserData, Long> {

}
