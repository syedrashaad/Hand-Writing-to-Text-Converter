import {Subject} from 'rxjs';
import { FileAndUrlModel } from '../models/FileAndUrl.model';


export class DisplayService
{
    displayInformation = new Subject<FileAndUrlModel>();
    alertInformation = new Subject<String>();
    
}