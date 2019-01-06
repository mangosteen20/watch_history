package mangosteen.watch_history;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

@Component
class CategoryResourceAssembler implements ResourceAssembler<Category, Resource<Category>> {

    @Override
    public Resource<Category> toResource(Category category) {

        return new Resource<>(category,
                linkTo(methodOn(CatController.class).one(category.getId())).withSelfRel(),
                linkTo(methodOn(CatController.class).all()).withRel("categories"));
    }
}
