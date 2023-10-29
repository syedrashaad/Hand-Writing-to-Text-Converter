import { Component, OnInit } from '@angular/core';
import { FileAndUrlModel } from '../models/FileAndUrl.model';
import { BackendRequest } from '../services/backendrequest.service';
import { DisplayService } from '../services/display.service';

@Component({
  selector: 'app-display-image',
  templateUrl: './display-image.component.html',
  styleUrls: ['./display-image.component.css']
})
export class DisplayImageComponent implements OnInit {

  filesAndUrls:FileAndUrlModel[]=[];
  nonAllowedFiles:String[]=[];
  pdfUrl:String='';
  loadingFlag=false;
  
  constructor(private displayService:DisplayService, private backendService:BackendRequest) { }

  ngOnInit(): void {
    this.displayService.displayInformation.subscribe(fileAndUrl =>
      {
        this.filesAndUrls.push(fileAndUrl);
      });

      this.displayService.alertInformation.subscribe(name=>{
        this.nonAllowedFiles.push(name);
      })

      
  }

  onDeleteClick(index)
  {
    this.filesAndUrls.splice(index,index+1);
  }

  onServerPush()
  { 
    this.loadingFlag = true;
    var currentTime = new Date().getTime();

    while (currentTime + 5000 >= new Date().getTime()) {
    }
   
    this.backendService.extractText(this.filesAndUrls).subscribe(url=>{
     this.pdfUrl = url;
    }, error=>{
    },()=>
    {
      this.loadingFlag = false;
    });

  }

  onDownload()
  {
    this.filesAndUrls.splice(0);
    this.pdfUrl='';
  }
}
