package mangosteen.watch_history;

public class CategoryNotFoundException extends RuntimeException {
    CategoryNotFoundException(Long id) {
        super("Cound not find category " + id);
    }
}
