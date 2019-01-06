package mangosteen.watch_history;

import org.springframework.data.jpa.repository.JpaRepository;


// This will be AUTO IMPLEMENTED by Spring into a Bean called CatRepository

public interface CatRepository extends JpaRepository<Category, Long> {

}