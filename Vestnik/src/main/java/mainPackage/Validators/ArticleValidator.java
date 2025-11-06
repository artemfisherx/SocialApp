package mainPackage.Validators;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import mainPackage.Entities.VcArticle;

public class ArticleValidator implements Validator{
	
	@Override
	public boolean supports(Class<?> clazz)
	{
		return VcArticle.class.equals(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors)
	{			
		VcArticle article = (VcArticle) target;
				 
		if(article.getTitle().length()<10)
			errors.rejectValue("title", "validation.error.article.title.length");
		
		if(article.getText().length()<20)
			errors.rejectValue("text", "validation.error.article.text.length");
		
	}

}
