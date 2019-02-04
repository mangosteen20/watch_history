package mangosteen.watch_history;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;


@RestController
public class CatController {

    @Autowired
    private final CatRepository cat_repo;

    private final CategoryResourceAssembler cat_assem;

    private List<Category> cat_top3;    // 3 categories watched most

    CatController(CatRepository repository, CategoryResourceAssembler assembler) {
        this.cat_repo = repository;
        this.cat_assem = assembler;
        this.cat_top3 = findTop3();
    }

    /* find top 3 categories by using a max heap */
    private List<Category> findTop3() {
        List<Category> top3 = new ArrayList<Category>();

        if (cat_repo == null) {
            return top3;
        }

        List<Category> categories = cat_repo.findAll();
        PriorityQueue<Category> cat_max = new PriorityQueue<Category>(new Comparator<Category>() {
            @Override
            public int compare(Category cat1, Category cat2) {
                return cat2.getTime() - cat1.getTime();
            }
        });
        for (Category cat: categories) {
            cat_max.add(cat);
        }

        int i = 0;
        while (!cat_max.isEmpty() && i <= 2) {
            top3.add(cat_max.poll());
            i++;
        }
        while(i <= 2) {
            top3.add(new Category());
            i++;
        }
        return top3;
    }

    /* get all categories */
    @GetMapping("/categories")
    Resources<Resource<Category>> all(){
        List<Resource<Category>> categories = cat_repo.findAll().stream()
                .map(cat_assem::toResource)
                .collect(Collectors.toList());
        return new Resources<>(categories,
                linkTo(methodOn(CatController.class).all()).withSelfRel());
    }

    /* get top 3 categories */
    @GetMapping("/top3")
    Resources<Resource<Category>> top3(){
        cat_top3 = findTop3();
        List<Resource<Category>> categories = cat_top3.stream()
                .map(cat_assem::toResource)
                .collect(Collectors.toList());
        return new Resources<>(categories,
                linkTo(methodOn(CatController.class).top3()).withSelfRel());
    }


    /* post an unseen category */
    @PostMapping("/categories")
    ResponseEntity<?> newCategory(@RequestBody Category newCategory) throws URISyntaxException {
        Resource<Category> resource = cat_assem.toResource(cat_repo.save(newCategory));

        return ResponseEntity
                .created(new URI(resource.getId().expand().getHref()))
                .body(resource);
    }

    /* get a single item by id */
    @GetMapping("/categories/{id}")
    Resource<Category> one(@PathVariable Long id) {
        Category cat = cat_repo.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return cat_assem.toResource(cat);
    }

    /* put a single item by id */
    @PutMapping("/categories/{id}")
    ResponseEntity<?> replaceCategory(@RequestBody Category newCategory, @PathVariable Long id) throws URISyntaxException {
        Category updatedCategory = cat_repo.findById(id)
                .map(category -> {
                    category.setName(newCategory.getName());
                    category.setTime(category.getTime()+newCategory.getTime());
                    return cat_repo.save(category);
                })
                .orElseGet(() -> {
                    newCategory.setId(id);
                    return cat_repo.save(newCategory);
                });
        Resource<Category> resource = cat_assem.toResource(updatedCategory);

        return ResponseEntity
                .created(new URI(resource.getId().expand().getHref()))
                .body(resource);
    }

    /* delete a single item by id */
    @DeleteMapping("/categories/{id}")
    ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        cat_repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}