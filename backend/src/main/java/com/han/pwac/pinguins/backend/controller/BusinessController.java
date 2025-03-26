package com.han.pwac.pinguins.backend.controller;

import com.han.pwac.pinguins.backend.annotations.OAuth2;
import com.han.pwac.pinguins.backend.annotations.Sanitation;
import com.han.pwac.pinguins.backend.domain.DTO.BusinessDto;
import com.han.pwac.pinguins.backend.domain.DTO.FileDto;
import com.han.pwac.pinguins.backend.domain.DTO.IValidate;
import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.services.BusinessService;
import com.han.pwac.pinguins.backend.services.UserService;
import com.han.pwac.pinguins.backend.services.UserTokenService;
import com.han.pwac.pinguins.backend.services.contract.IBaseService;
import com.han.pwac.pinguins.backend.services.contract.IFileService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/business")
public class BusinessController {
    private final BusinessService businessService;
    private final IFileService fileUploadService;
    private final MimeType VALID_IMAGE_MIME_TYPE = new MimeType("image", "*");
    private final UserTokenService userTokenService;

    @Autowired
    public BusinessController(BusinessService businessService, IFileService fileUploadService, UserTokenService userTokenService) {
        this.businessService = businessService;
        this.fileUploadService = fileUploadService;
        this.userTokenService = userTokenService;
    }

    @GetMapping("/{businessId}")
    public ResponseEntity<BusinessDto> getBusinessById(@PathVariable int businessId) {
        return businessService.findById(businessId)
                .map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @OAuth2
    @Sanitation
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> editBusinessById(@Length(max=BusinessDto.NAME_LENGTH) @RequestParam String name,
                                                   @RequestParam String description,
                                                   @RequestParam(value = "photos", required = false) Optional<MultipartFile> image,
                                                   @Length(max=BusinessDto.LOCATION_LENGTH) @RequestParam String location,
                                                   UserInfo userInfo
    ) {
        // user should always be logged in as business
        int businessId = userTokenService.getVerificationByProviderId(Optional.of(userInfo.id())).getBusinessId().get();

        Optional<String> fileDirectory = image.map(file ->
            fileUploadService.uploadFile(file, VALID_IMAGE_MIME_TYPE))
                // business should always be found the user was logged in as one
                .or(() -> businessService.findById(businessId).get().photo().path());

        BusinessDto updatedBusiness = new BusinessDto(businessId, name, description, new FileDto(fileDirectory), location);

        if (businessService.update(businessId, updatedBusiness)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @OAuth2
    @GetMapping("/email/all")
    public String[] getAllBusinessEmails(UserInfo userInfo) {
        int businessId = userTokenService.getVerificationByProviderId(Optional.of(userInfo.id())).getBusinessId().get();

        return businessService.getAllEmails(businessId);
    }
    
    @OAuth2
    @PostMapping
    public ResponseEntity<Object> createBusiness(@Length(max=BusinessDto.NAME_LENGTH) @RequestBody String name,
                                                 UserInfo userInfo
    ) {
        BusinessDto business = new BusinessDto(0, name, "", null, "");
        if (businessService.add(business)) {
            return ResponseEntity.ok(businessService.getLastInsertedId());
        }
        return ResponseEntity.notFound().build();
    }
}
