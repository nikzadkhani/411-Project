import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PlaylistService } from 'app/entities/playlist/playlist.service';
import { IPlaylist, Playlist } from 'app/shared/model/playlist.model';

describe('Service Tests', () => {
  describe('Playlist Service', () => {
    let injector: TestBed;
    let service: PlaylistService;
    let httpMock: HttpTestingController;
    let elemDefault: IPlaylist;
    let expectedResult: IPlaylist | IPlaylist[] | boolean | null;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
      });
      expectedResult = null;
      injector = getTestBed();
      service = injector.get(PlaylistService);
      httpMock = injector.get(HttpTestingController);

      elemDefault = new Playlist(0, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA');
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign({}, elemDefault);

        service.find(123).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(elemDefault);
      });

      it('should create a Playlist', () => {
        const returnedFromService = Object.assign(
          {
            id: 0
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.create(new Playlist()).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should update a Playlist', () => {
        const returnedFromService = Object.assign(
          {
            syncifyId: 'BBBBBB',
            name: 'BBBBBB',
            author: 'BBBBBB'
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.update(expected).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should return a list of Playlist', () => {
        const returnedFromService = Object.assign(
          {
            syncifyId: 'BBBBBB',
            name: 'BBBBBB',
            author: 'BBBBBB'
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.query().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should delete a Playlist', () => {
        service.delete(123).subscribe(resp => (expectedResult = resp.ok));

        const req = httpMock.expectOne({ method: 'DELETE' });
        req.flush({ status: 200 });
        expect(expectedResult);
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
