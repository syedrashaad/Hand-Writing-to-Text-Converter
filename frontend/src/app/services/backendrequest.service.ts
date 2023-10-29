import {HttpClient, HttpParams} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FileAndUrlModel } from '../models/FileAndUrl.model';

@Injectable()
export class BackendRequest
{
    constructor(private httpClient:HttpClient)
    {

    }

    extractText(filesAndUrls:FileAndUrlModel[])
    {
       let files:File[]=[];

       let fd = new FormData();
        for(let fileAnrUrl of filesAndUrls)
        {
            files.push(fileAnrUrl.file);
            fd.append('image',fileAnrUrl.file);
        }

        return this.httpClient.post("http://localhost:8081/handnote/extract-text",fd,{responseType:'text'});

     

    }

    storeIntoS3(file):any
    {
        let locator;
        var fd = new FormData();
        fd.append('image', file);

        return this.httpClient.post("http://localhost:8081/handnote/store-s3",fd,{responseType: 'text'});
  
        
    }
}