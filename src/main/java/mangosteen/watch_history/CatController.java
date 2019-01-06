package mangosteen.watch_history;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
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

    CatController(CatRepository repository, CategoryResourceAssembler assembler) {
        this.cat_repo = repository;
        this.cat_assem = assembler;
    }

    @GetMapping("/categories")
    Resources<Resource<Category>> all(){
        List<Resource<Category>> categories = cat_repo.findAll().stream()
                .map(cat_assem::toResource)
                .collect(Collectors.toList());
        return new Resources<>(categories,
                linkTo(methodOn(CatController.class).all()).withSelfRel());
    }

    @PostMapping("/categories")
    ResponseEntity<?> newCategory(@RequestBody Category newCategory) throws URISyntaxException {
        Resource<Category> resource = cat_assem.toResource(cat_repo.save(newCategory));

        return ResponseEntity
                .created(new URI(resource.getId().expand().getHref()))
                .body(resource);
    }

    //Single item

    @GetMapping("/categories/{id}")
    Resource<Category> one(@PathVariable Long id) {
        Category cat = cat_repo.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return cat_assem.toResource(cat);
    }

    @PutMapping("/categories/{id}")
    ResponseEntity<?> replaceCategory(@RequestBody Category newCategory, @PathVariable Long id) throws URISyntaxException {
        Category updatedCategory = cat_repo.findById(id)
                .map(category -> {
                    category.setName(newCategory.getName());
                    category.setFreq(newCategory.getFreq());
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

    @DeleteMapping("/categories/{id}")
    ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        cat_repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}