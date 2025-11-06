package mainPackage.Controllers;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Valid;
import mainPackage.Entities.VcArticle;
import mainPackage.Formatters.OffsetDateTimeFormatter;
import mainPackage.Interfaces.IRepository;
import mainPackage.Validators.ArticleValidator;

@RequestMapping("/article")
@Controller
public class ArticleController {
	
	@Inject
	@Named("mainRepository")
	IRepository repo;
	
	@InitBinder
	protected void init(WebDataBinder binder)
	{
		binder.addValidators(new ArticleValidator());
		binder.addCustomFormatter(new OffsetDateTimeFormatter() {
			@Override
			public String print(OffsetDateTime dt, Locale locale)
			{
				return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
			}		
		});
	}
	
	@GetMapping("/add")
	String addArticle(@ModelAttribute("article") VcArticle article, Model model)	
	{		
		if(article==null)
			article = new VcArticle();
		
		model.addAttribute("article", article);		
		return "addEditArticle";
	}
	
	@GetMapping("/edit/{id}")
	String editArticle(@PathVariable("id") int id, Model model) throws Exception
	{
		var article = repo.getVcArticle(id);
		if(article!=null)
		{
			model.addAttribute("article", article);		
			return "addEditArticle";
		}
		
		return "redirect:/";
	}
	
	@PostMapping("/save")
	String saveArticle(@Valid VcArticle article, BindingResult result, Model model, RedirectAttributes attr)
	{	
		if(result.hasErrors())
		{
			//attr.addFlashAttribute("article", article);				
			//return "redirect:/article/add";
			
			model.addAttribute("article", article);
			return "addEditArticle";
		}
		
		return "redirect:/";
	}

}
